package ch.ergon.arciphant.sca

import ch.ergon.arciphant.dsl.ArciphantProjectDsl
import ch.ergon.arciphant.util.SimpleTask
import ch.ergon.arciphant.util.projectHierarchy
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault(because = "The task validates source files and does not produce outputs.")
internal abstract class PackageStructureValidationTask @Inject constructor(
    objects: ObjectFactory,
) : SimpleTask() {
    @get:Input
    abstract val expectedPackage: Property<String>

    @get:Input
    abstract val excludedSourceFolders: SetProperty<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sourceFiles: ConfigurableFileTree = objects.fileTree()

    @TaskAction
    fun validatePackageStructure() {
        logger.info("Validate package structure of project '$projectPath'.")
        val excludedSourceFolderPatterns = excludedSourceFolders.get().map { "src/$it/**" }
        logger.info("Exclude source folders: {}", excludedSourceFolderPatterns)

        val correctSourceFolderPattern = "src/*/*/${expectedPackage.get()}/**"
        logger.info("Expected source folder: {}", correctSourceFolderPattern)

        val invalidFiles = sourceFiles.matching {
            excludedSourceFolderPatterns.forEach { exclude(it) }
            exclude(correctSourceFolderPattern)
        }.files

        if (invalidFiles.isNotEmpty()) {
            invalidFiles.sortedBy { it.path }.forEach {
                logger.error("Source file '${it.path}' has invalid package name.")
            }
            throw GradleException("There are source files with invalid package names. See error log above.")
        } else {

            logger.info("Package structure of project '$projectPath' is valid.")
        }
    }
}

internal fun Project.registerValidatePackageStructureTask(settings: GlobalPackageStructureValidationSettings) {
    if (project.path == project.rootProject.path) {
        project.registerValidatePackageStructureAggregateTask()
    } else {
        project.registerValidatePackageStructureExecutionTask(settings)
    }
}

private fun Project.registerValidatePackageStructureAggregateTask() {
    tasks.register(VALIDATE_PACKAGE_STRUCTURE_TASK) {
        group = GROUP
        description = "Validates the package structure of all projects"

        dependsOn(subprojects.map { "${it.path}:$VALIDATE_PACKAGE_STRUCTURE_TASK" })
    }
}

private fun Project.registerValidatePackageStructureExecutionTask(settings: GlobalPackageStructureValidationSettings) {
    val projectPath = path
    val expectedPackageProvider = provider { settings.determinePackageFor(packageConfigHierarchy()) }
    tasks.register(VALIDATE_PACKAGE_STRUCTURE_TASK, PackageStructureValidationTask::class.java) {
        group = GROUP
        description = "Validates the package structure of project '$projectPath'."
        expectedPackage.set(expectedPackageProvider)
        enabled = projectPath !in settings.excludedProjectPaths
        excludedSourceFolders.set(settings.excludedSourceFolders)
        sourceFiles.from(projectDir)
        sourceFiles.include("src/**")
    }
}

/**
 * Collects the project-level arciphant configuration of this project and all its parent projects
 * (excluding the root project), ordered from the topmost parent down to this project.
 */
private fun Project.packageConfigHierarchy(): List<ProjectPackageStructureValidationSettings> = projectHierarchy().map {
        val dsl = it.extensions.getByType(ArciphantProjectDsl::class.java)
        ProjectPackageStructureValidationSettings(it.name, dsl.packageName, dsl.absolutePackageName)
    }

private const val VALIDATE_PACKAGE_STRUCTURE_TASK = "validatePackageStructure"
private const val GROUP = "verification"
