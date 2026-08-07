package ch.ergon.arciphant.sca

import org.gradle.api.Project
import java.io.File

internal class PackageStructureValidator(private val settings: PackageStructureValidationSettings) {

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
        return settings.excludedProjectPaths.contains(path)
    }

    private fun doValidate(project: Project): Set<File> {
        val invalidFiles = project.fileTree(project.projectDir) {
            // include all files in src-folders
            include("src/**")

            // exclude files from source folders excluded
            val excludedSourceFolderPatterns = settings.excludedSourceFolders.map { "src/$it/**" }
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
        val projectDsl = arciphantProjectDsl()
        projectDsl?.absolutePackagePath()?.let { return it }
        if (projectDsl?.relativePackagePath() == null) {
            settings.absolutePackagePathsByProjectPath[path]?.let { return it }
        }
        return packageFragments().withBasePackage()
    }

    private fun String.withBasePackage(): String {
        return if(settings.basePackagePath != null) "${settings.basePackagePath}/$this" else this
    }

    private fun Project.packageFragments(): String {
        // the root project does not contribute a package fragment
        return generateSequence(this) { it.parent }
            .takeWhile { it.parent != null }
            .toList()
            .asReversed()
            .mapNotNull { it.toPackageFragment() }
            .joinToString("/")
    }

    private fun Project.toPackageFragment(): String? {
        val configuredPackageFragment = arciphantProjectDsl()?.relativePackagePath()
            ?: settings.relativePackagePathsByProjectName[name]
        if (configuredPackageFragment != null) return configuredPackageFragment.ifEmpty { null }
        return name.toDefaultPackageFragment()
    }

    private fun String.toDefaultPackageFragment(): String {
        val packageFragment = if (settings.useLowerCase) this.lowercase() else this
        return settings.removedSpecialCharacters.fold(packageFragment) { fragment, character ->
            fragment.replace(character, "")
        }
    }
}
