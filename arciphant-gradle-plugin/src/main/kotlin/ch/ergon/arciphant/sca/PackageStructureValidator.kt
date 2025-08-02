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
            include("src/*/**")

            // exclude files excluded in config
            config.excludedSourceFolders.forEach { exclude(it) }

            // exclude correct files
            exclude("src/*/${project.toPackagePath()}/**")
        }
        return invalidFiles.toSet()
    }

    private fun Project.toPackagePath(): String {
        val configuredAbsolutePath = config.absolutePackagesByProjectPath[path]
        if (configuredAbsolutePath != null) return configuredAbsolutePath

        return "${config.basePackageName}.${toPackageFragment()}"
    }

    private fun Project.toPackageFragment(): String {
        val configuredPackageFragment = config.relativePackagesByProjectName[name]
        if (configuredPackageFragment != null) return configuredPackageFragment

        val packageFragment = if (config.useLowerCase) name.lowercase() else name
        return config.removedSpecialCharacters.fold(packageFragment) { fragment, character ->
            fragment.replace(character, "")
        }
    }
}
