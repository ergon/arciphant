package ch.ergon.arciphant.sca

import org.gradle.api.Project
import java.io.File

internal class PackageStructureValidator(private val config: PackageStructureValidationConfig) {

    fun validate(project: Project): PackageStructureValidationResult {
        if(project.isExcluded()) {
            return ValidPackageStructure
        }
        val invalidFiles = doValidate(project)
        if(invalidFiles.isEmpty()) {
            return ValidPackageStructure
        }
        return InvalidPackageStructure(invalidFiles)
    }

    private fun Project.isExcluded(): Boolean {
        return config.excludedProjectPaths.contains(path)
    }

    private fun doValidate(project: Project): Set<File> {
        val invalidFiles = project.fileTree(project.projectDir) {
            // include all files in src-folders
            include("src/**")

            // exclude files from source folders excluded
            val excludedSourceFolderPatterns = config.excludedSourceFolders.map { "src/$it/**" }
            project.logger.info("Exclude source folders: {}", excludedSourceFolderPatterns)
            excludedSourceFolderPatterns.forEach { exclude(it) }

            // exclude files in correct packages
            val correctSourceFolderPattern = "src/*/*/${project.toPackagePath()}/**"
            project.logger.info("Expected source folder: {}", correctSourceFolderPattern)
            exclude(correctSourceFolderPattern)
        }
        return invalidFiles.toSet()
    }

    private fun Project.toPackagePath(): String {
        val configuredAbsolutePath = config.absolutePackagePathsByProjectPath[path]
        return configuredAbsolutePath ?: "${config.basePackagePath}/${projectPathToPackageFragments()}"
    }

    private fun Project.projectPathToPackageFragments(): String {
        return project.path.replaceFirst(":", "").split(":")
            .mapNotNull { it.projectNameToPackageFragment() }
            .joinToString("/")
    }

    private fun String.projectNameToPackageFragment(): String? {
        val configuredPackageFragment = config.relativePackagePathsByProjectName[this]
        if (configuredPackageFragment != null) return if(configuredPackageFragment != "") configuredPackageFragment else null

        val packageFragment = if (config.useLowerCase) this.lowercase() else this
        return config.removedSpecialCharacters.fold(packageFragment) { fragment, character ->
            fragment.replace(character, "")
        }
    }
}
