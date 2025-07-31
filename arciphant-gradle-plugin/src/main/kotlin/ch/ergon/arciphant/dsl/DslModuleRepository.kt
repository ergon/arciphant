package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.*

internal class DslModuleRepository(private val dsl: ArciphantDsl) : ModuleRepository {

    override fun load() = dsl.functionalModules.map { it.create() } + dsl.bundleModules.map { it.createBundleModule() }

    private fun FunctionalModuleBuilder.create(): FunctionalModule {
        val components = build().toSet()
        return when (this) {
            is LibraryModuleBuilder -> LibraryModule(LibraryModuleReference(name), components)
            is DomainModuleBuilder -> DomainModule(DomainModuleReference(name), components)
        }
    }

    private fun FunctionalModuleBuilder.build(): List<Component> {
        return build(inheritedComponents = structures.flatMap { it.build() })
    }

    private fun ComponentStructureBuilder.build(): List<Component> {
        return build(inheritedComponents = basedOn?.build() ?: emptyList())

    }

    private fun ComponentContainerBuilder.build(inheritedComponents: List<Component>): List<Component> {
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
            reference = BundleModuleReference(name),
            plugin = plugin?.let { Plugin(it) },
            includes = includes.ifEmpty { dsl.functionalModules }.map { it.reference }.toSet()
        )
    }

}
