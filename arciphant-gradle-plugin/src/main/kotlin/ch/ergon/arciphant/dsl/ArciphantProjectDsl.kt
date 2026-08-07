package ch.ergon.arciphant.dsl

open class ArciphantProjectDsl {

    /**
     * Replaces the package fragment derived from this project's name.
     * The settings-level `basePackageName` still applies; only the fragment related to this project is replaced.
     * Like [PackageStructureValidationSettingsDsl.mapProjectNamesToPackageFragments], the replacement also
     * affects the expected packages of all subprojects.
     *
     * Example: with `basePackageName("com.company.project")` and `packageName = "ppa"` configured in
     * `accounting/payment-provider-adapter/build.gradle.kts`:
     * ```
     * Gradle project path                   | Absolute package name
     * --------------------------------------|-----------------------------------
     * :accounting:payment-provider-adapter | com.company.project.accounting.ppa
     * ```
     * An empty string drops this project's fragment from the expected package entirely.
     *
     * Has no effect on the root project (the root project does not contribute a package fragment) —
     * use [absolutePackageName] there instead.
     */
    var packageName: String? = null

    /**
     * Completely overrides the expected package name for this project.
     * Other than with [packageName], the settings-level `basePackageName` is NOT used,
     * and subprojects are not affected.
     *
     * Example: `absolutePackageName = "com.specific.package.name"`
     */
    var absolutePackageName: String? = null
}
