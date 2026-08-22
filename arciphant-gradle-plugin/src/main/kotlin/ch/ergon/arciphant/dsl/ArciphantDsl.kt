package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.core.ComponentLayout
import ch.ergon.arciphant.core.ComponentLayout.PROJECT
import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
import ch.ergon.arciphant.util.verifyName

open class ArciphantDsl {

    internal var globalBasePath: String? = null
    internal var disableFolderCreation: Boolean = false
    internal var disableQualifiedArchiveBaseName: Boolean? = null
    internal var componentLayout: ComponentLayout = PROJECT
    internal var withTestSourceSet: Boolean? = null
    internal var withTestFixturesSourceSet: Boolean? = null
    internal var testSourceSetName: ((String) -> String)? = null
    internal var testFixturesSourceSetName: ((String) -> String)? = null

    internal val functionalModules = mutableListOf<FunctionalModuleBuilder>()
    internal val bundleModules = mutableSetOf<BundleModuleBuilder>()

    internal val packageStructureValidation = PackageStructureValidationBuilder()

    fun basePath(basePath: String) {
        globalBasePath = basePath
    }

    fun disableFolderCreation() {
        disableFolderCreation = true
    }

    fun disableQualifiedArchiveBaseName() {
        disableQualifiedArchiveBaseName = true
    }

    fun projectSetComponentLayout() = componentLayout(PROJECT)

    fun sourceSetComponentLayout() = componentLayout(SOURCE_SET)

    private fun componentLayout(componentLayout: ComponentLayout) {
        this.componentLayout = componentLayout
    }

    fun withTestSourceSet(withTestSourceSet: Boolean) {
        this.withTestSourceSet = withTestSourceSet
    }

    fun withTestFixturesSourceSet(withTestFixturesSourceSet: Boolean) {
        this.withTestFixturesSourceSet = withTestFixturesSourceSet
    }

    fun testSourceSetName(testSourceSetName: (String) -> String) {
        this.testSourceSetName = testSourceSetName
    }

    fun testFixturesSourceSetName(testFixturesSourceSetName: (String) -> String) {
        this.testFixturesSourceSetName = testFixturesSourceSetName
    }

    fun template(): ModuleTemplateBuilder {
        return ModuleTemplateBuilder()
    }

    fun library(name: String, basePath: String? = null, template: ModuleTemplateBuilder) = library(name, basePath, setOf(template))

    fun library(name: String, basePath: String? = null, templates: Set<ModuleTemplateBuilder> = emptySet()): FunctionalModuleBuilder {
        verifyName(name, "library")
        return FunctionalModuleBuilder(name, basePath, templates, FunctionalModuleType.LIBRARY).also { functionalModules.add(it) }
    }

    fun module(name: String, basePath: String? = null, template: ModuleTemplateBuilder) = module(name, basePath, setOf(template))

    fun module(name: String, basePath: String? = null, templates: Set<ModuleTemplateBuilder> = emptySet()): FunctionalModuleBuilder {
        verifyName(name, "module")
        return FunctionalModuleBuilder(name, basePath, templates, FunctionalModuleType.DOMAIN).also { functionalModules.add(it) }
    }

    fun bundle(
        name: String,
        basePath: String? = null,
        plugin: String? = null,
        includes: Set<ModuleBuilder> = emptySet()
    ): BundleModuleBuilder {
        return BundleModuleBuilder(
            name = name,
            basePath = basePath,
            plugin = plugin,
            includes = includes
        ).also { bundleModules.add(it) }
    }

    fun packageStructureValidation(block: PackageStructureValidationDsl.() -> Unit) {
        packageStructureValidation.block()
    }

}
