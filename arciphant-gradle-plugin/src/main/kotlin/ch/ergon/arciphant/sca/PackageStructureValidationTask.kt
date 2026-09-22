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
    val resources: Boolean,
) : Serializable

/**
 * Validates that every source file lies in one of the project's source directories, below the expected
 * package of that directory. The source directories are the actual directories of the project's source
 * sets, so relocated directories (e.g. via 'customizeAllComponents') are validated at their location.
 * Files under 'src/' that do not belong to any source set are reported as well.
 */
@DisableCachingByDefault(because = "The task validates source files and does not produce outputs.")
internal abstract class PackageStructureValidationTask @Inject constructor(
    private val objects: ObjectFactory,
) : SimpleTask() {
    /** The directories of the project's source sets and the expected package of each. */
    @get:Input
    abstract val validatedSourceDirectories: ListProperty<ValidatedSourceDirectory>

    /** Absolute roots configured via 'excludeSrcFolders'; their files are skipped. */
    @get:Input
    abstract val excludedSourceRoots: SetProperty<String>

    /** Whether files in resources directories are skipped ('excludeResourcesFolder'). */
    @get:Input
    abstract val excludeResourcesFolders: Property<Boolean>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sourceFiles: ConfigurableFileTree = objects.fileTree()

    @TaskAction
    fun validatePackageStructure() {
        logger.info("Validate package structure of project '$projectPath'.")
        val directories = validatedSourceDirectories.get()
            .map { SourceDirectory(File(it.directoryPath).toPath(), it.expectedPackageSegments(), it.resources) }
            .sortedByDescending { it.path.nameCount } // the innermost directory owns a file of nested directories
        directories.forEach {
            logger.info("Expected package '{}' in source directory '{}'.", it.packageSegments.joinToString("/"), it.path)
        }
        val excludedRoots = excludedSourceRoots.get().map { File(it).toPath() }

        val invalidPackageFiles = mutableListOf<File>()
        val unassignedFiles = mutableListOf<File>()
        collectSourceFiles(directories).forEach { file ->
            val path = file.toPath()
            if (excludedRoots.any { path.startsWith(it) }) return@forEach
            val directory = directories.firstOrNull { path.startsWith(it.path) }
            when {
                directory == null -> unassignedFiles.add(file)
                directory.resources && excludeResourcesFolders.get() -> {}
                !directory.containsInExpectedPackage(path) -> invalidPackageFiles.add(file)
            }
        }

        invalidPackageFiles.sortedBy { it.path }.forEach {
            logger.error("Source file '${it.path}' has invalid package name.")
        }
        unassignedFiles.sortedBy { it.path }.forEach {
            logger.error("Source file '${it.path}' does not belong to any source set.")
        }
        if (invalidPackageFiles.isNotEmpty() || unassignedFiles.isNotEmpty()) {
            throw GradleException("There are source files with invalid package names. See error log above.")
        }
        logger.info("Package structure of project '$projectPath' is valid.")
    }

    /** All files under 'src/' plus the files of source directories located elsewhere. */
    private fun collectSourceFiles(directories: List<SourceDirectory>): Set<File> {
        val files = mutableSetOf<File>()
        files += sourceFiles.files
        directories.forEach { directory ->
            val tree = objects.fileTree()
            tree.setDir(directory.path.toFile())
            files += tree.files
        }
        return files
    }

    private data class SourceDirectory(
        val path: Path,
        val packageSegments: List<String>,
        val resources: Boolean,
    ) {
        /** True when the file lies below this directory's expected package. */
        fun containsInExpectedPackage(file: Path): Boolean {
            val relative = path.relativize(file)
            return relative.nameCount > packageSegments.size &&
                packageSegments.withIndex().all { (index, segment) -> relative.getName(index).toString() == segment }
        }
    }

    private fun ValidatedSourceDirectory.expectedPackageSegments() =
        expectedPackagePath.split("/").filter { it.isNotEmpty() }
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
        validatedSourceDirectories.set(
            project.provider { resolveValidatedSourceDirectories(project, settings, validatedProject, projectPath) }
        )
        excludedSourceRoots.set(
            settings.excludedSrcFolders.map { projectDir.resolve("src/$it").absolutePath }.toSet()
        )
        excludeResourcesFolders.set(settings.excludeResourcesFolder)
        sourceFiles.from(projectDir)
        sourceFiles.include("src/**")
    }
}

private fun resolveValidatedSourceDirectories(
    project: Project,
    settings: PackageStructureValidationSettings,
    validatedProject: ValidatedProject?,
    projectPath: String,
): List<ValidatedSourceDirectory> {
    val sourceSets = project.extensions.findByName("sourceSets") as? SourceSetContainer ?: return emptyList()
    val componentBySourceSetName = validatedProject?.componentSourceSets.orEmpty()
        .flatMap { component -> component.sourceSetNames.map { it to component.componentName } }
        .toMap()
    val buildDir = project.layout.buildDirectory.get().asFile.toPath()

    return sourceSets.flatMap { sourceSet ->
        val expectedPackage =
            settings.determinePackageFor(projectPath, validatedProject, componentBySourceSetName[sourceSet.name])
        val resourceDirs = sourceSet.resources.srcDirs
        sourceSet.sourceDirectories()
            .filterNot { it.toPath().startsWith(buildDir) } // generated sources are not validated
            .map { ValidatedSourceDirectory(it.absolutePath, expectedPackage, resources = it in resourceDirs) }
    }.distinctBy { it.directoryPath }
}

/** All source directories of the source set: java and resources, plus the Kotlin directories if present. */
private fun SourceSet.sourceDirectories(): Set<File> {
    val kotlinDirs = ((this as? ExtensionAware)?.extensions?.findByName("kotlin") as? SourceDirectorySet)
        ?.srcDirs ?: emptySet()
    return allSource.srcDirs + kotlinDirs
}

private const val VALIDATE_PACKAGE_STRUCTURE_TASK = "validatePackageStructure"
private const val GROUP = "verification"
