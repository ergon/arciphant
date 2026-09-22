package ch.ergon.arciphant.sca

import java.io.Serializable

internal data class PackageStructureValidationSettings(
    val basePackagePath: String?,
    val useLowerCase: Boolean,
    val removedSpecialCharacters: Set<String>,
    val relativePackagePathsByProjectName: Map<String, String>,
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
        val componentFragment = componentName?.mappedPackageFragment()
        return listOfNotNull(projectPackage.takeIf { it.isNotEmpty() }, componentFragment).joinToString("/")
    }

    /**
     * Determines the source folder patterns that contain correctly packaged files. Without component
     * source sets (project layout, or a project without components such as a bundle), every source set is
     * validated against the project's package. With component source sets (functional module in the source
     * set layout), each component source set is validated against the component's package, while the
     * standard 'main' and 'test' source sets are validated against the module's package.
     */
    fun determineValidSourceFolderPatterns(projectPath: String, project: ValidatedProject?): Set<String> {
        val componentSourceSets = project?.componentSourceSets
        if (componentSourceSets == null) {
            return setOf(sourceFolderPattern("*", determinePackageFor(projectPath, project)))
        }
        val modulePackage = determinePackageFor(projectPath, project)
        val standardSourceSetPatterns = setOf(
            sourceFolderPattern("main", modulePackage),
            sourceFolderPattern("test", modulePackage),
        )
        val componentSourceSetPatterns = componentSourceSets.flatMap { component ->
            val componentPackage = determinePackageFor(projectPath, project, component.componentName)
            component.sourceSetNames.map { sourceFolderPattern(it, componentPackage) }
        }
        return standardSourceSetPatterns + componentSourceSetPatterns
    }

    private fun projectPackagePath(projectPath: String, project: ValidatedProject?): String {
        val fragments = if (project == null) {
            projectPath
                .replaceFirst(":", "")
                .split(":")
                .map { it.normalizedPackageFragment() }
        } else {
            project.basePathFragments.map { it.normalizedPackageFragment() } +
                project.mappableNames.mapNotNull { it.mappedPackageFragment() }
        }
        return fragments.joinToString("/").withBasePackage()
    }

    private fun sourceFolderPattern(sourceSetName: String, packagePath: String) =
        "src/$sourceSetName/*/$packagePath/**"

    /** Maps a module or component name to its package fragment, honoring the configured name mappings. */
    private fun String.mappedPackageFragment(): String? {
        val configuredPackageFragment = relativePackagePathsByProjectName[this]
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
