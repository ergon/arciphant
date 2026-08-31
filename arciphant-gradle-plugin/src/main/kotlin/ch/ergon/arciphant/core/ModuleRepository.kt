package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.ComponentLayout.PROJECT
import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
import ch.ergon.arciphant.core.model.*
import ch.ergon.arciphant.dsl.*
import ch.ergon.arciphant.dsl.FunctionalModuleType.DOMAIN
import ch.ergon.arciphant.dsl.FunctionalModuleType.LIBRARY
import ch.ergon.arciphant.util.verify

internal class ModuleRepository(private val dsl: ArciphantDsl) {

    fun load() = dsl.functionalModules.map { it.create() } + dsl.bundleModules.map { it.createBundleModule() }

    private fun FunctionalModuleBuilder.create(): FunctionalModule {
        val components = build()
        components.validate()
        return when (moduleType) {
            LIBRARY -> LibraryModule(this.reference(), components)
            DOMAIN -> DomainModule(this.reference(), components)
        }
    }

    private fun FunctionalModuleBuilder.build(): Set<Component> {
        return componentsBuilder.build(inheritedComponents = templates.flatMap { it.build() })
    }

    private fun ModuleTemplateBuilder.build(): Set<Component> {
        return componentsBuilder.build(inheritedComponents = extends.flatMap { it.build() })

    }

    private fun ComponentsBuilder.build(inheritedComponents: List<Component>): Set<Component> {
        val componentsByName = (inheritedComponents + components).toDistinctMap()
        componentDependencyOverrides.forEach { (componentName, dependencies) ->
            val existingComponent = componentsByName.getOrThrow(componentName)
            componentsByName[componentName] = existingComponent.addDependencies(dependencies)
        }
        return componentsByName.values.toSet()
    }

    private fun List<Component>.toDistinctMap(): MutableMap<String, Component> {
        val componentsByName = mutableMapOf<String, Component>()
        forEach {
            verify(componentsByName.putIfAbsent(it.reference.name, it) == null) {
                "Component with name '${it.reference.name}' has already been declared. Use 'extendComponent' instead of 'createComponent' to extend an existing component."
            }
        }
        return componentsByName
    }

    private fun Map<String, Component>.getOrThrow(componentName: String): Component {
        val existingComponent = get(componentName)
        verify(existingComponent != null) {
            "Component with name '$componentName' does not exist. Use 'createComponent' instead of 'extendComponent' to create a new component."
        }
        return existingComponent
    }

    private fun Component.addDependencies(additionalDependencies: Set<Dependency>) = Component(
        reference = reference,
        plugin = plugin,
        dependsOn = dependsOn + additionalDependencies,
        withTestSourceSet = withTestSourceSet,
        withTestFixturesSourceSet = withTestFixturesSourceSet,
    )

    private fun Collection<Component>.validate() = forEach { it.validate() }

    private fun Component.validate() {
        when (dsl.componentLayout) {
            PROJECT -> {
                PROJECT.verifyConfig(this::withTestSourceSet)
                PROJECT.verifyConfig(this::withTestFixturesSourceSet)
            }
            SOURCE_SET -> {
                SOURCE_SET.verifyConfig(this::plugin)
            }
        }
    }

    private fun BundleModuleBuilder.createBundleModule(): BundleModule {
        return BundleModule(
            reference = reference(),
            plugin = plugin?.let { Plugin(it) },
            includes = includes.ifEmpty { dsl.functionalModules }.map { it.reference() }.toSet()
        ).also { it.validate() }
    }

    private fun BundleModule.validate() {
        when (dsl.componentLayout) {
            PROJECT -> {
                // no forbidden config
            }
            SOURCE_SET -> {
                SOURCE_SET.verifyConfig(this::plugin)
            }
        }
    }

    private fun ModuleBuilder.reference() = ModuleReference(
        parentProjectPath = (basePath ?: dsl.globalBasePath)?.split(":", "/").orEmpty(),
        name = name
    )

}
