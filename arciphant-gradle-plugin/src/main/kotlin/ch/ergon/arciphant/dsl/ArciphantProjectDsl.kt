package ch.ergon.arciphant.dsl

open class ArciphantProjectDsl {

    /**
     * Overrides the package fragment of this project when validating the package structure
     * (task `validatePackageStructure`).
     * The base package name and the package fragments of the parent projects are still used;
     * only the fragment derived from this project's name is replaced.
     *
     * Example: with base package name 'com.company.project', setting `packageName = "ppa"` on the project
     * `:accounting:payment-provider-adapter` results in the expected package 'com.company.project.accounting.ppa'.
     *
     * An empty string removes the fragment of this project from the expected package.
     */
    var packageName: String? = null

    /**
     * Completely overrides the expected package of this project when validating the package structure
     * (task `validatePackageStructure`).
     * Other than with [packageName], neither the base package name nor the package fragments of the
     * parent projects are used.
     *
     * The override is inherited by child projects: their expected package is the configured absolute
     * package name extended by their own package fragments (including possible [packageName] overrides).
     * A child project can replace an inherited absolute package name by configuring its own
     * [absolutePackageName].
     *
     * Example: with `absolutePackageName = "my.special"` on the project `:backend:accounting`, the expected
     * package of `:backend:accounting` is 'my.special' and the expected package of `:backend:accounting:domain`
     * is 'my.special.domain'.
     */
    var absolutePackageName: String? = null

}
