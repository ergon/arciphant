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
