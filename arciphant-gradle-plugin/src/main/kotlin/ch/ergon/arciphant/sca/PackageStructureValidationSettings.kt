package ch.ergon.arciphant.sca

import java.io.Serializable

internal data class GlobalPackageStructureValidationSettings(
    val basePackagePath: String?,
    val useLowerCase: Boolean,
    val removedSpecialCharacters: Set<String>,
    val excludedProjectPaths: Set<String>,
    val excludedSourceFolders: Set<String>,
) : Serializable {

    /**
     * Determines the expected package of the last project in [projectHierarchy].
     *
     * The nearest project in the hierarchy with a configured absolute package name (including the project
     * itself) replaces the base package and the package fragments of all projects above it. The package
     * fragments of the projects below it are appended as usual.
     */
    fun determinePackageFor(projectHierarchy: List<ProjectPackageStructureValidationSettings>): String {
        val projectWithAbsolutePath = projectHierarchy.withIndex().lastOrNull { it.value.absolutePackageName != null }
        val absolutePath = projectWithAbsolutePath?.value?.absolutePackageName?.packageToFolderPath() ?: basePackagePath
        val packageFragments = projectHierarchy
            .drop(projectWithAbsolutePath?.index?.plus(1) ?: 0)
            .mapNotNull { it.toPackageFragment() }
        return (listOfNotNull(absolutePath) + packageFragments).joinToString("/")
    }

    private fun ProjectPackageStructureValidationSettings.toPackageFragment(): String? {
        if (packageName != null) {
            return packageName.ifEmpty { null }?.packageToFolderPath()
        }
        val packageFragment = if (useLowerCase) projectName.lowercase() else projectName
        return removedSpecialCharacters.fold(packageFragment) { fragment, character ->
            fragment.replace(character, "")
        }
    }
}

internal data class ProjectPackageStructureValidationSettings(
    val projectName: String,
    val packageName: String?,
    val absolutePackageName: String?,
)

internal fun String.packageToFolderPath() = replace(".", "/")
