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
     * Determines the expected package (as folder path) for the given project — with [componentName],
     * for the given component of that project (source set layout): the component name is mapped to a
     * package fragment with the same rules as project names and appended to the project's package.
     */
    fun determinePackageFor(projectPath: String, componentName: String? = null): String {
        val projectPackage = absolutePackagePathsByProjectPath[projectPath]
            ?: projectPath
                .replaceFirst(":", "")
                .split(":")
                .mapNotNull { it.nameToPackageFragment() }
                .joinToString("/")
                .withBasePackage()
        val componentFragment = componentName?.nameToPackageFragment()
        return listOfNotNull(projectPackage.takeIf { it.isNotEmpty() }, componentFragment).joinToString("/")
    }

    /**
     * Determines the source folder patterns that contain correctly packaged files. Without
     * [componentSourceSets] (project layout, or a project without components such as a bundle), every
     * source set is validated against the project's package. With [componentSourceSets] (functional
     * module in the source set layout), each component source set is validated against the component's
     * package, while the standard 'main' and 'test' source sets are validated against the module's package.
     */
    fun determineValidSourceFolderPatterns(
        projectPath: String,
        componentSourceSets: List<ComponentSourceSets>?,
    ): Set<String> {
        if (componentSourceSets == null) {
            return setOf(sourceFolderPattern("*", determinePackageFor(projectPath)))
        }
        val modulePackage = determinePackageFor(projectPath)
        val standardSourceSetPatterns = setOf(
            sourceFolderPattern("main", modulePackage),
            sourceFolderPattern("test", modulePackage),
        )
        val componentSourceSetPatterns = componentSourceSets.flatMap { component ->
            val componentPackage = determinePackageFor(projectPath, component.componentName)
            component.sourceSetNames.map { sourceFolderPattern(it, componentPackage) }
        }
        return standardSourceSetPatterns + componentSourceSetPatterns
    }

    private fun sourceFolderPattern(sourceSetName: String, packagePath: String) =
        "src/$sourceSetName/*/$packagePath/**"

    private fun String.nameToPackageFragment(): String? {
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
