package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.core.model.*
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.util.verify
import ch.ergon.arciphant.util.verifyName

class BundleModuleBuilder internal constructor(
    name: String,
    basePath: String?,
    internal val plugin: String?,
    internal val includes: Set<ModuleBuilder>
) : ModuleBuilder(name, basePath)

internal enum class FunctionalModuleType {
    LIBRARY, DOMAIN
}

class FunctionalModuleBuilder internal constructor(
    name: String,
    basePath: String?,
    internal val templates: Set<ModuleTemplateBuilder>,
    internal val moduleType: FunctionalModuleType,
) : ModuleBuilder(name, basePath) {
    internal val componentsBuilder = ComponentsBuilder()

    /**
     * [dependencies] become implementation dependencies of the component; [apiDependencies] become api
     * dependencies, which are additionally exposed to the component's consumers. The two sets are separate —
     * a component listed in [apiDependencies] does not need to be repeated in [dependencies].
     */
    fun createComponent(
        name: String,
        plugin: String? = null,
        dependencies: Set<String> = emptySet(),
        apiDependencies: Set<String> = emptySet(),
        withTestSourceSet: Boolean? = null,
        withTestFixturesSourceSet: Boolean? = null,
    ): FunctionalModuleBuilder {
        componentsBuilder.doCreateComponent(
            name = name,
            plugin = plugin,
            apiDependencies = apiDependencies,
            implementationDependencies = dependencies,
            withTestSourceSet = withTestSourceSet,
            withTestFixturesSourceSet = withTestFixturesSourceSet,
        )
        return this
    }

    fun extendComponent(
        name: String,
        dependencies: Set<String> = emptySet(),
        apiDependencies: Set<String> = emptySet(),
    ): FunctionalModuleBuilder {
        componentsBuilder.doExtendComponent(
            name = name,
            apiDependencies = apiDependencies,
            implementationDependencies = dependencies,
        )
        return this
    }

}

class ModuleTemplateBuilder internal constructor() {
    internal val componentsBuilder = ComponentsBuilder()

    internal val extends = mutableListOf<ModuleTemplateBuilder>()

    fun extends(template: ModuleTemplateBuilder): ModuleTemplateBuilder {
        extends.add(template)
        return this
    }

    /**
     * [dependencies] become implementation dependencies of the component; [apiDependencies] become api
     * dependencies, which are additionally exposed to the component's consumers. The two sets are separate —
     * a component listed in [apiDependencies] does not need to be repeated in [dependencies].
     */
    fun createComponent(
        name: String,
        plugin: String? = null,
        dependencies: Set<String> = emptySet(),
        apiDependencies: Set<String> = emptySet(),
        withTestSourceSet: Boolean? = null,
        withTestFixturesSourceSet: Boolean? = null,
    ): ModuleTemplateBuilder {
        componentsBuilder.doCreateComponent(
            name = name,
            plugin = plugin,
            apiDependencies = apiDependencies,
            implementationDependencies = dependencies,
            withTestSourceSet = withTestSourceSet,
            withTestFixturesSourceSet = withTestFixturesSourceSet,
        )
        return this
    }

    fun extendComponent(
        name: String,
        dependencies: Set<String> = emptySet(),
        apiDependencies: Set<String> = emptySet(),
    ): ModuleTemplateBuilder {
        componentsBuilder.doExtendComponent(
            name = name,
            apiDependencies = apiDependencies,
            implementationDependencies = dependencies,
        )
        return this
    }
}

internal class ComponentsBuilder {
    internal val components = mutableListOf<Component>()
    internal val componentDependencyOverrides = mutableMapOf<String, Set<Dependency>>()

    fun doCreateComponent(
        name: String,
        plugin: String?,
        apiDependencies: Set<String>,
        implementationDependencies: Set<String>,
        withTestSourceSet: Boolean?,
        withTestFixturesSourceSet: Boolean?,
    ) {
        verifyName(name, "component")
        val dependencies = mapDependencies(apiDependencies, implementationDependencies)
        components.add(
            Component(
                reference = ComponentReference(name),
                plugin = plugin?.let { Plugin(it) },
                dependsOn = dependencies,
                withTestSourceSet = withTestSourceSet,
                withTestFixturesSourceSet = withTestFixturesSourceSet,
            )
        )
    }

    fun doExtendComponent(name: String, apiDependencies: Set<String>, implementationDependencies: Set<String>) {
        val dependencies = mapDependencies(apiDependencies, implementationDependencies)
        verify(componentDependencyOverrides.putIfAbsent(name, dependencies) == null) {
            "Component '$name' has already been extended in the current context."
        }
    }

    private fun mapDependencies(apiDependencies: Set<String>, implementationDependencies: Set<String>) =
        apiDependencies.toDependencies(API) + implementationDependencies.toDependencies(IMPLEMENTATION)

    private fun Set<String>.toDependencies(type: DependencyType) =
        map { Dependency(ComponentReference(it), type) }.toSet()
}

sealed class ModuleBuilder(internal val name: String, internal val basePath: String?)
