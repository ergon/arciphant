package ch.ergon.arciphant.sca

import ch.ergon.arciphant.util.SimpleTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.io.Serializable
import java.nio.file.Path
import javax.inject.Inject

/** A source directory whose files must live below the expected package. */
internal data class ValidatedSourceDirectory(
    val directoryPath: String,
    val expectedPackagePath: String,
) : Serializable

@DisableCachingByDefault(because = "The task validates source files and does not produce outputs.")
internal abstract class PackageStructureValidationTask @Inject constructor(
    private val objects: ObjectFactory,
) : SimpleTask() {
    /**
     * The directories of the project's source sets, validated against their expected package. Relocated
     * source directories (e.g. via 'customizeAllComponents') are validated at their actual location.
     * Empty when the project has no source sets — the 'src' tree scan then falls back to convention patterns.
     */
    @get:Input
    abstract val validatedSourceDirectories: ListProperty<ValidatedSourceDirectory>

    /**
     * Patterns (relative to the project directory) excluded from the 'src' tree scan: excluded folders,
     * the directories already validated per source set, and — without source sets — the convention
     * patterns of correctly packaged files.
     */
    @get:Input
    abstract val srcTreeExcludedPatterns: SetProperty<String>

    /** Absolute roots configured via 'excludeSrcFolders', also applied within validated source directories. */
    @get:Input
    abstract val excludedSourceRoots: SetProperty<String>

    /** True when the project's source sets were resolved — files outside of them are reported as unassigned. */
    @get:Input
    abstract val sourceSetsResolved: Property<Boolean>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sourceFiles: ConfigurableFileTree = objects.fileTree()

    @TaskAction
    fun validatePackageStructure() {
        logger.info("Validate package structure of project '$projectPath'.")
        val excludedRoots = excludedSourceRoots.get().map { File(it).toPath() }

        val invalidPackageFiles = validateSourceDirectories(excludedRoots)
        val unassignedFiles = validateSrcTree()

        invalidPackageFiles.sortedBy { it.path }.forEach {
            logger.error("Source file '${it.path}' has invalid package name.")
        }
        unassignedFiles.sortedBy { it.path }.forEach {
            if (sourceSetsResolved.get()) {
                logger.error("Source file '${it.path}' does not belong to any source set.")
            } else {
                logger.error("Source file '${it.path}' has invalid package name.")
            }
        }
        if (invalidPackageFiles.isNotEmpty() || unassignedFiles.isNotEmpty()) {
            throw GradleException("There are source files with invalid package names. See error log above.")
        }
        logger.info("Package structure of project '$projectPath' is valid.")
    }

    private fun validateSourceDirectories(excludedRoots: List<Path>): List<File> {
        return validatedSourceDirectories.get().flatMap { directory ->
            logger.info(
                "Expected package '{}' in source directory '{}'.",
                directory.expectedPackagePath,
                directory.directoryPath,
            )
            val tree = objects.fileTree()
            tree.setDir(File(directory.directoryPath))
            tree.matching {
                if (directory.expectedPackagePath.isEmpty()) {
                    exclude("**")
                } else {
                    exclude("${directory.expectedPackagePath}/**")
                }
            }.files.filter { file -> excludedRoots.none { file.toPath().startsWith(it) } }
        }
    }

    private fun validateSrcTree(): Set<File> {
        val excludedPatterns = srcTreeExcludedPatterns.get()
        logger.info("Exclude from 'src' tree scan: {}", excludedPatterns)
        return sourceFiles.matching {
            excludedPatterns.forEach { exclude(it) }
        }.files
    }
}

internal fun Project.registerValidatePackageStructureTask(
    settings: PackageStructureValidationSettings,
    validatedProject: ValidatedProject?,
) {
    if (project.path == project.rootProject.path) {
        project.registerValidatePackageStructureAggregateTask()
    } else {
        project.registerValidatePackageStructureExecutionTask(settings, validatedProject)
    }
}

private fun Project.registerValidatePackageStructureAggregateTask() {
    tasks.register(VALIDATE_PACKAGE_STRUCTURE_TASK) {
        group = GROUP
        description = "Validates the package structure of all projects"

        dependsOn(subprojects.map { "${it.path}:$VALIDATE_PACKAGE_STRUCTURE_TASK" })
    }
}

private fun Project.registerValidatePackageStructureExecutionTask(
    settings: PackageStructureValidationSettings,
    validatedProject: ValidatedProject?,
) {
    val projectPath = path
    tasks.register(VALIDATE_PACKAGE_STRUCTURE_TASK, PackageStructureValidationTask::class.java) {
        group = GROUP
        description = "Validates the package structure of project '$projectPath'."
        enabled = projectPath !in settings.excludedProjectPaths

        // resolved lazily: the task is configured after the project is evaluated, so customized source
        // directories and source sets added by plugins are final at that point
        val config = project.provider { resolveValidationConfig(project, settings, validatedProject, projectPath) }
        validatedSourceDirectories.set(config.map { it.validatedSourceDirectories })
        srcTreeExcludedPatterns.set(config.map { it.srcTreeExcludedPatterns })
        sourceSetsResolved.set(config.map { it.sourceSetsResolved })
        excludedSourceRoots.set(
            settings.excludedSrcFolders.map { projectDir.resolve("src/$it").absolutePath }.toSet()
        )
        sourceFiles.from(projectDir)
        sourceFiles.include("src/**")
    }
}

private data class ValidationConfig(
    val validatedSourceDirectories: List<ValidatedSourceDirectory>,
    val srcTreeExcludedPatterns: Set<String>,
    val sourceSetsResolved: Boolean,
)

private fun resolveValidationConfig(
    project: Project,
    settings: PackageStructureValidationSettings,
    validatedProject: ValidatedProject?,
    projectPath: String,
): ValidationConfig {
    val excludedFolderPatterns = settings.excludedSrcFolders.map { "src/$it/**" }.toSet() +
        setOfNotNull(if (settings.excludeResourcesFolder) "src/*/resources/**" else null)

    val sourceSets = project.extensions.findByName("sourceSets") as? SourceSetContainer
        ?: return ValidationConfig(
            validatedSourceDirectories = emptyList(),
            srcTreeExcludedPatterns = excludedFolderPatterns +
                settings.determineValidSourceFolderPatterns(projectPath, validatedProject),
            sourceSetsResolved = false,
        )

    val componentBySourceSetName = validatedProject?.componentSourceSets.orEmpty()
        .flatMap { component -> component.sourceSetNames.map { it to component.componentName } }
        .toMap()
    val projectDir = project.projectDir.toPath()
    val buildDir = project.layout.buildDirectory.get().asFile.toPath()
    val excludedRoots = settings.excludedSrcFolders.map { projectDir.resolve("src").resolve(it) }

    val sourceDirectories = mutableSetOf<File>()
    val validatedDirectories = sourceSets.flatMap { sourceSet ->
        val expectedPackage =
            settings.determinePackageFor(projectPath, validatedProject, componentBySourceSetName[sourceSet.name])
        val resourceDirs = sourceSet.resources.srcDirs
        sourceSet.sourceDirectories()
            .onEach { sourceDirectories.add(it) }
            .filterNot { it.toPath().startsWith(buildDir) }
            .filterNot { settings.excludeResourcesFolder && it in resourceDirs }
            .filterNot { dir -> excludedRoots.any { dir.toPath().startsWith(it) } }
            .map { ValidatedSourceDirectory(it.absolutePath, expectedPackage) }
    }.distinctBy { it.directoryPath }

    // every source set directory below the project directory is covered by its own validation (or an
    // exclusion) and therefore not part of the 'src' tree scan for unassigned files
    val sourceDirectoryPatterns = sourceDirectories.mapNotNull { dir ->
        val path = dir.toPath()
        if (path.startsWith(projectDir) && path != projectDir) {
            projectDir.relativize(path).joinToString("/") + "/**"
        } else {
            null
        }
    }.toSet()

    return ValidationConfig(
        validatedSourceDirectories = validatedDirectories,
        srcTreeExcludedPatterns = excludedFolderPatterns + sourceDirectoryPatterns,
        sourceSetsResolved = true,
    )
}

/** All source directories of the source set: java and resources, plus the Kotlin directories if present. */
private fun SourceSet.sourceDirectories(): Set<File> {
    val kotlinDirs = ((this as? ExtensionAware)?.extensions?.findByName("kotlin") as? SourceDirectorySet)
        ?.srcDirs ?: emptySet()
    return allSource.srcDirs + kotlinDirs
}

private const val VALIDATE_PACKAGE_STRUCTURE_TASK = "validatePackageStructure"
private const val GROUP = "verification"
