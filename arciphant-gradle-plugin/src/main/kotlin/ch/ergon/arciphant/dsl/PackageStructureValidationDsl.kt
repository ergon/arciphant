package ch.ergon.arciphant.dsl

/**
 * Configuring how project-names should be mapped to package names.
 *
 * Example:
 * ```
 * Gradle project path                  | Absolute package name
 * -------------------------------------|-----------------------------------------------
 * :certificate:domain                  | com.company.project.certificate.domain
 * :certificate:web-api                 | com.company.project.package.certificate.webapi
 * :accounting:domain                   | com.company.project.package.accounting.domain
 * :accounting:web-api                  | com.company.project.package.accounting.webapi
 * :accounting:payment-provider-adapter | com.company.project.package.accounting.ppa
 * ```
 */
sealed interface PackageStructureValidationDsl {

    /**
     * The base package name for the whole project.
     *
     * @param basePackageName the base package name, e.g. 'com.company.project'
     */
    fun basePackageName(basePackageName: String)

    /**
     * By default, upper case letters are converted to lower case when mapping project names to corresponding package fragments.
     *
     * Example: project name 'FileStore' is mapped to package fragment 'filestore'
     *
     * Use [disableUseLowerCase] to deactivate this behavior.
     */
    fun disableUseLowerCase()

    /**
     * By default, underscores '_' are removed when mapping project name to corresponding package fragment.
     *
     * Example: project name 'file_store' is mapped to package fragment 'filestore'.
     *
     * Use [disableRemoveUnderscore] to deactivate this behavior.
     */
    fun disableRemoveUnderscore()

    /**
     * By default, hyphens '-' are removed when mapping project name to corresponding package fragment.
     *
     * Example: project name 'file-store' is mapped to package fragment 'filestore'.
     *
     * Use [disableRemoveHyphen] to deactivate this behavior.
     */
    fun disableRemoveHyphen()

    /**
     * Configure mappings for specific project names.
     * The project name can be either a leaf project (e.g., an arciphant component) or a parent project (e.g., an arciphant module).
     * The [basePackageName] is still used. The configured value replaces only the package fragment related to the specified project.
     *
     * Example:
     * ```
     * basePackageName("com.company.project")
     * mapProjectNameToPackageFragment(
     *   "financial-accounting" to "accounting",
     *   "payment-provider-adapter", "ppa",
     * )
     * ```
     * Above config results in the following mapping:
     * ```
     * Gradle project path                            | Absolute package name
     * -----------------------------------------------|--------------------------------------
     * :financial-accounting:domain                   | com.company.project.accounting.domain
     * :financial-accounting:web-api                  | com.company.project.accounting.webapi
     * :financial-accounting:payment-provider-adapter | com.company.project.accounting.ppa
     * ```
     */
    fun mapProjectNamesToPackageFragments(vararg projectNameToPackageFragment: Pair<String, String>)

    /**
     * Completely overrides the package name for the given Gradle project path.
     * Other than with [mapProjectNamesToPackageFragments], the [basePackageName] is NOT used.
     *
     * Example:
     * ```
     * mapProjectPathToAbsolutePackage(
     *   ":specific:project:path" to "com.specific.package.name",
     *   ":any:other:path" to "com.any.other.package.name",
     * )
     * ```
     */
    fun mapProjectPathsToAbsolutePackages(vararg projectPathToAbsolutePackage: Pair<String, String>)

    /**
     * Excludes a specific project from package validation.
     *
     * @param projectPath the project path in the Gradle dot notation, e.g. ':specific:project:path'
     */
    fun excludeProjectPath(projectPath: String)

    /**
     * By default, all folders in the src-folder of each project are validated.
     * Use [excludeResourcesFolder] to exclude the resource folder ('src/main/resources') from validation.
     */
    fun excludeResourcesFolder()

    /**
     * By default, all folders in the src-folder of each project are validated.
     * Use [excludeSrcFolders] to exclude specific folders.
     *
     * Examples:
     * To exclude 'src/generated' use: excludedSrcFolder("generated")
     * To exclude 'src/main/generated' use: excludedSrcFolder("main/generated")
     */
    fun excludeSrcFolders(folderName: String)
}
