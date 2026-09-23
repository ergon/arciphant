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
     *
     * An absolute package mapping of the exact project path takes precedence. Otherwise, an absolute package
     * mapping of the module path replaces the module package, so it also applies to the components of the
     * module (in both component layouts).
     */
    fun determinePackageFor(
        projectPath: String,
        project: ValidatedProject?,
        componentName: String? = null,
    ): String {
        val projectPackage = absolutePackagePathsByProjectPath[projectPath]
            ?: project?.managedProjectPackagePath()
            ?: unmanagedProjectPackagePath(projectPath)
        return packagePathOf(projectPackage, componentName?.mappedPackageFragment(packageFragmentsByComponentName))
    }

    private fun ValidatedProject.managedProjectPackagePath(): String {
        val modulePackage = absolutePackagePathsByProjectPath[modulePath]
            ?: packagePathOf(
                basePackagePath,
                *basePathFragments.map { it.normalizedPackageFragment() }.toTypedArray(),
                moduleName.mappedPackageFragment(packageFragmentsByModuleName),
            )
        return packagePathOf(modulePackage, componentName?.mappedPackageFragment(packageFragmentsByComponentName))
    }

    private fun unmanagedProjectPackagePath(projectPath: String): String {
        val fragments = projectPath.removePrefix(":").split(":").map { it.normalizedPackageFragment() }
        return packagePathOf(basePackagePath, *fragments.toTypedArray())
    }

    /** Joins the given package fragments (as folder paths), skipping missing and empty ones. */
    private fun packagePathOf(vararg fragments: String?): String {
        return fragments.filterNot { it.isNullOrEmpty() }.joinToString("/")
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
}
