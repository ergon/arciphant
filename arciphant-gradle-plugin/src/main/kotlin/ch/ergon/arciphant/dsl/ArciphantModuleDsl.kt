package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.*
import ch.ergon.arciphant.model.DependencyType.API
import ch.ergon.arciphant.model.DependencyType.IMPLEMENTATION

class BundleModuleBuilder internal constructor(
    internal val name: String,
    internal val plugin: String?,
    internal val includes: Set<ModuleBuilder>
) : ModuleBuilder

internal enum class FunctionalModuleType {
    LIBRARY, DOMAIN
}

class FunctionalModuleBuilder internal constructor(
    internal val name: String,
    internal val templates: Set<ModuleTemplateBuilder>,
    internal val moduleType: FunctionalModuleType,
) : ModuleBuilder, ComponentContainerBuilder<FunctionalModuleBuilder>() {

    fun createComponent(
        name: String,
        plugin: String? = null,
        dependsOnApi: Set<String> = emptySet(),
        dependsOn: Set<String> = emptySet(),
    ): FunctionalModuleBuilder {
        doCreateComponent(name = name, plugin = plugin, dependsOnApi = dependsOnApi, dependsOn = dependsOn)
        return this
    }

    fun extendComponent(
        name: String,
        dependsOnApi: Set<String> = emptySet(),
        dependsOn: Set<String> = emptySet(),
    ): FunctionalModuleBuilder {
        doExtendComponent(name = name, dependsOnApi = dependsOnApi, dependsOn = dependsOn)
        return this
    }

}

class ModuleTemplateBuilder internal constructor() : ComponentContainerBuilder<ModuleTemplateBuilder>() {

    internal val extends = mutableListOf<ModuleTemplateBuilder>()

    fun extends(template: ModuleTemplateBuilder): ModuleTemplateBuilder {
        extends.add(template)
        return this
    }

    fun createComponent(
        name: String,
        plugin: String? = null,
        dependsOnApi: Set<String> = emptySet(),
        dependsOn: Set<String> = emptySet(),
    ): ModuleTemplateBuilder {
        doCreateComponent(name = name, plugin = plugin, dependsOnApi = dependsOnApi, dependsOn = dependsOn)
        return this
    }

    fun extendComponent(
        name: String,
        dependsOnApi: Set<String> = emptySet(),
        dependsOn: Set<String> = emptySet(),
    ): ModuleTemplateBuilder {
        doExtendComponent(name = name, dependsOnApi = dependsOnApi, dependsOn = dependsOn)
        return this
    }
}

sealed class ComponentContainerBuilder<B : ComponentContainerBuilder<B>> {
    internal val components = mutableListOf<Component>()
    internal val componentDependencyOverrides = mutableMapOf<String, Set<Dependency>>()

    protected fun doCreateComponent(name: String, plugin: String?, dependsOnApi: Set<String>, dependsOn: Set<String>) {
        verifyName(name, "component")
        val dependencies = mapDependencies(dependsOnApi, dependsOn)
        components.add(Component(ComponentReference(name), plugin?.let { Plugin(it) }, dependencies))
    }

    protected fun doExtendComponent(name: String, dependsOnApi: Set<String>, dependsOn: Set<String>) {
        val dependencies = mapDependencies(dependsOnApi, dependsOn)
        verify(componentDependencyOverrides.putIfAbsent(name, dependencies) == null) {
            "Component '$name' has already been extended in the current context."
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun castedThis() = this as B

    private fun mapDependencies(apiDependencies: Set<String>, implementationDependencies: Set<String>) =
        apiDependencies.toDependencies(API) + implementationDependencies.toDependencies(IMPLEMENTATION)

    private fun Set<String>.toDependencies(type: DependencyType) = map { Dependency(ComponentReference(it), type) }.toSet()
}

sealed interface ModuleBuilder

internal val ModuleBuilder.reference
    get() = when (this) {
        is FunctionalModuleBuilder -> ModuleReference(name)
        is BundleModuleBuilder -> ModuleReference(name)
    }
