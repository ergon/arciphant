package ch.cbossi.gradle.modulith

internal data class ModulithConfiguration(
    val modules: List<Module>,
) {
    val libraries by lazy { modules.filter { it.isLibrary } }
}

internal data class Module(
    val name: String,
    val isLibrary: Boolean,
    val components: List<Component>,
) {
    init {
        require(components.isNotEmpty()) {
            "Module '$name' has no components. " +
                    "This is not allowed, since the module gradle project is implicitly created through its components"
        }
    }

    fun hasComponent(component: Component) = components.map { it.reference }.contains(component.reference)
}

internal data class Component(
    val reference: ComponentReference,
    val plugin: Plugin,
    val dependsOn: Collection<Dependency>,
) {
    val name = reference.name
}

data class ComponentReference internal constructor(internal val name: String)

internal data class Dependency(val component: ComponentReference, val type: DependencyType)

internal enum class DependencyType(val configurationName: String) {
    IMPLEMENTATION("implementation"),
    API("api")
}

internal data class Plugin(val id: String)
