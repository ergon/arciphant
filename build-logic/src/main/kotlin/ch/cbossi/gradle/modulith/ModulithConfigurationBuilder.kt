package ch.cbossi.gradle.modulith

sealed class ModulithConfigurationBuilder {
    internal val components = mutableListOf<ComponentReference>()
    internal val dependencies = mutableListOf<ComponentDependency>()
    internal val componentPlugins = mutableMapOf<ComponentReference, Plugin>()

    fun addComponent(name: String) = addComponent(ComponentReference(name))

    fun addComponent(component: ComponentReference): ComponentReference {
        components.add(component)
        return component
    }

    /**
     * Overrides the base plugin for this component.
     * Important: Since the base plugin is overridden, the plugin itself should apply the base plugin
     */
    fun ComponentReference.withPlugin(id: String): ComponentReference {
        componentPlugins[this] = Plugin(id)
        return this
    }

    fun ComponentReference.dependsOnApi(vararg components: ComponentReference): ComponentReference {
        return dependsOn(components, DependencyType.API)
    }

    fun ComponentReference.dependsOn(vararg components: ComponentReference): ComponentReference {
        return dependsOn(components, DependencyType.IMPLEMENTATION)
    }

    private fun ComponentReference.dependsOn(
        components: Array<out ComponentReference>,
        type: DependencyType
    ): ComponentReference {
        dependencies.addAll(components.map { ComponentDependency(this, type, it) })
        return this
    }
}

class AllModulesConfigurationBuilder : ModulithConfigurationBuilder()

class ModuleConfigurationBuilder(
    internal val reference: ModuleReference,
    internal val isLibrary: Boolean,
) : ModulithConfigurationBuilder() {
    internal var removeAllModulesComponents: Boolean = false
    internal val removedAllModulesComponents = mutableListOf<ComponentReference>()

    fun removeAllModulesComponents() {
        removeAllModulesComponents = true
    }

    fun removeComponent(vararg component: ComponentReference) {
        removedAllModulesComponents.addAll(component)
    }
}

internal data class ComponentDependency(
    val source: ComponentReference,
    val type: DependencyType,
    val dependsOn: ComponentReference,
)


