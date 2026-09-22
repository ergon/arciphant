package ch.ergon.arciphant.sca

import java.io.Serializable

internal data class PackageStructureValidationSettings(
    val basePackagePath: String?,
    val useLowerCase: Boolean,
    val removedSpecialCharacters: Set<String>,
    val packageFragmentsByModuleName: Map<String, String>,
    val packageFragmentsByComponentName: Map<String, String>,
    val absolutePackagePathsByProjectPath: Map<String, String>,
    val excludedProjectPaths: Set<String>,
    val excludedSrcFolders: Set<String>,
    val excludeResourcesFolder: Boolean,
) : Serializable {

    /**
     * Determines the expected package (as folder path) for the given project. For an Arciphant-managed
     * [project], the configured name mappings apply only to its module and component names; base path
     * segments are merely normalized. For an unmanaged project (null), all path segments are normalized
     * without name mappings. With [componentName] (a component of a source set layout module), the
     * component's package fragment is appended.
     */
    fun determinePackageFor(
        projectPath: String,
        project: ValidatedProject?,
        componentName: String? = null,
    ): String {
        val projectPackage = absolutePackagePathsByProjectPath[projectPath]
            ?: projectPackagePath(projectPath, project)
        val componentFragment = componentName?.mappedPackageFragment(packageFragmentsByComponentName)
        return listOfNotNull(projectPackage.takeIf { it.isNotEmpty() }, componentFragment).joinToString("/")
    }

    private fun projectPackagePath(projectPath: String, project: ValidatedProject?): String {
        val fragments = if (project == null) {
            projectPath
                .replaceFirst(":", "")
                .split(":")
                .map { it.normalizedPackageFragment() }
        } else {
            project.basePathFragments.map { it.normalizedPackageFragment() } +
                listOfNotNull(
                    project.moduleName.mappedPackageFragment(packageFragmentsByModuleName),
                    project.componentName?.mappedPackageFragment(packageFragmentsByComponentName),
                )
        }
        return fragments.joinToString("/").withBasePackage()
    }

    /** Maps a module or component name to its package fragment, honoring the given name mappings. */
    private fun String.mappedPackageFragment(packageFragmentsByName: Map<String, String>): String? {
        val configuredPackageFragment = packageFragmentsByName[this]
        if (configuredPackageFragment != null) {
            return configuredPackageFragment.ifEmpty { null }
        }
        return normalizedPackageFragment()
    }

    /** Normalizes a path segment (lower case, removed special characters) without applying name mappings. */
    private fun String.normalizedPackageFragment(): String {
        val packageFragment = if (useLowerCase) lowercase() else this
        return removedSpecialCharacters.fold(packageFragment) { fragment, character ->
            fragment.replace(character, "")
        }
    }

    private fun String.withBasePackage(): String {
        return if (basePackagePath != null) "$basePackagePath/$this" else this
    }
}
