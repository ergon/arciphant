package ch.ergon.arciphant.dsl

/**
 * Configuring how project-names should be mapped to package names.
 *
 * Example:
 * ```
 * Gradle project path                  | Absolute package name
 * -------------------------------------|----------------------------------------
 * :certificate:domain                  | com.company.project.certificate.domain
 * :certificate:web-api                 | com.company.project.certificate.webapi
 * :accounting:domain                   | com.company.project.accounting.domain
 * :accounting:web-api                  | com.company.project.accounting.webapi
 * :accounting:payment-provider-adapter | com.company.project.accounting.ppa
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
     * Configure mappings for specific module names. The mappings apply only to the names of arciphant
     * modules (in both component layouts) — not to base path segments or other projects.
     * The [basePackageName] is still used. The configured value replaces only the package fragment related to the
     * specified module.
     *
     * Example:
     * ```
     * basePackageName("com.company.project")
     * mapModuleNamesToPackageFragments("financial-accounting" to "accounting")
     * ```
     * Above config results in the following mapping:
     * ```
     * Gradle project path           | Absolute package name
     * ------------------------------|--------------------------------------
     * :financial-accounting:domain  | com.company.project.accounting.domain
     * :financial-accounting:web-api | com.company.project.accounting.webapi
     * ```
     */
    fun mapModuleNamesToPackageFragments(vararg moduleNameToPackageFragment: Pair<String, String>)

    /**
     * Configure mappings for specific component names. The mappings apply only to the names of arciphant
     * components (in both component layouts) — not to modules, base path segments or other projects.
     * The [basePackageName] is still used. The configured value replaces only the package fragment related to the
     * specified component.
     *
     * Example:
     * ```
     * basePackageName("com.company.project")
     * mapComponentNamesToPackageFragments("payment-provider-adapter" to "ppa")
     * ```
     * Above config results in the following mapping:
     * ```
     * Gradle project path                  | Absolute package name
     * -------------------------------------|----------------------------------
     * :accounting:domain                   | com.company.project.accounting.domain
     * :accounting:payment-provider-adapter | com.company.project.accounting.ppa
     * ```
     */
    fun mapComponentNamesToPackageFragments(vararg componentNameToPackageFragment: Pair<String, String>)

    /**
     * Completely overrides the package name for the given Gradle project path.
     * Other than with the name mappings, the [basePackageName] is NOT used.
     * In the source set layout, the override applies to the module project; the component fragments are still
     * appended for the component source sets.
     *
     * Example:
     * ```
     * mapProjectPathsToAbsolutePackages(
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
     * Use [excludeResourcesFolder] to exclude the resources folder of every source set
     * (e.g. 'src/main/resources') from validation.
     */
    fun excludeResourcesFolder()

    /**
     * By default, all folders in the src-folder of each project are validated.
     * Use [excludeSrcFolders] to exclude specific folders.
     *
     * Examples:
     * To exclude 'src/generated' use: excludeSrcFolders("generated")
     * To exclude 'src/main/generated' use: excludeSrcFolders("main/generated")
     */
    fun excludeSrcFolders(folderName: String)
}
