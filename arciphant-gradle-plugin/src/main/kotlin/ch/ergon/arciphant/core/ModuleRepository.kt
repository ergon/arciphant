package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.model.BundleModule
import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.Dependency
import ch.ergon.arciphant.core.model.DomainModule
import ch.ergon.arciphant.core.model.FunctionalModule
import ch.ergon.arciphant.core.model.LibraryModule
import ch.ergon.arciphant.core.model.ModuleReference
import ch.ergon.arciphant.core.model.Plugin
import ch.ergon.arciphant.dsl.*
import ch.ergon.arciphant.dsl.FunctionalModuleType.DOMAIN
import ch.ergon.arciphant.dsl.FunctionalModuleType.LIBRARY
import ch.ergon.arciphant.util.verify

internal class ModuleRepository(private val dsl: ArciphantDsl) {

    fun load() = dsl.functionalModules.map { it.create() } + dsl.bundleModules.map { it.createBundleModule() }

    private fun FunctionalModuleBuilder.create(): FunctionalModule {
        val components = build().toSet()
        return when (moduleType) {
            LIBRARY -> LibraryModule(this.reference(), components)
            DOMAIN -> DomainModule(this.reference(), components)
        }
    }

    private fun FunctionalModuleBuilder.build(): List<Component> {
        return componentsBuilder.build(inheritedComponents = templates.flatMap { it.build() })
    }

    private fun ModuleTemplateBuilder.build(): List<Component> {
        return componentsBuilder.build(inheritedComponents = extends.flatMap { it.build() })

    }

    private fun ComponentsBuilder.build(inheritedComponents: List<Component>): List<Component> {
        val componentsByName = (inheritedComponents + components).toDistinctMap()
        componentDependencyOverrides.forEach { (componentName, dependencies) ->
            val existingComponent = componentsByName.getOrThrow(componentName)
            componentsByName[componentName] = existingComponent.addDependencies(dependencies)
        }
        return componentsByName.values.toList()
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
        dependsOn = dependsOn + additionalDependencies
    )

    private fun BundleModuleBuilder.createBundleModule(): BundleModule {
        return BundleModule(
            reference = reference(),
            plugin = plugin?.let { Plugin(it) },
            includes = includes.ifEmpty { dsl.functionalModules }.map { it.reference() }.toSet()
        )
    }

    private fun ModuleBuilder.reference() = ModuleReference(parentProjectPath = emptyList(), name = name)

}
