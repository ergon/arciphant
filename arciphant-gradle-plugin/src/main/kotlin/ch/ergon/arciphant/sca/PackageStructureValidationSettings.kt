package ch.ergon.arciphant.sca

import java.io.Serializable

internal data class PackageStructureValidationSettings(
    val basePackagePath: String?,
    val useLowerCase: Boolean,
    val removedSpecialCharacters: Set<String>,
    val relativePackagePathsByProjectName: Map<String, String>,
    val absolutePackagePathsByProjectPath: Map<String, String>,
    val excludedProjectPaths: Set<String>,
    val excludedSourceFolders: Set<String>,
) : Serializable {

    fun determinePackageFor(projectPath: String): String {
        val configuredAbsolutePath = absolutePackagePathsByProjectPath[projectPath]
        return configuredAbsolutePath ?: projectPath
            .replaceFirst(":", "")
            .split(":")
            .mapNotNull { it.projectNameToPackageFragment() }
            .joinToString("/")
            .withBasePackage()
    }

    private fun String.projectNameToPackageFragment(): String? {
        val configuredPackageFragment = relativePackagePathsByProjectName[this]
        if (configuredPackageFragment != null) {
            return configuredPackageFragment.ifEmpty { null }
        }
        val packageFragment = if (useLowerCase) lowercase() else this
        return removedSpecialCharacters.fold(packageFragment) { fragment, character ->
            fragment.replace(character, "")
        }
    }

    private fun String.withBasePackage(): String {
        return if (basePackagePath != null) "$basePackagePath/$this" else this
    }
}
