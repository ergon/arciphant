package ch.cbossi.gradle.playground.build

internal data class ModulithConfiguration(
    val modules: List<Module>,
)

internal data class Module(
    val name: String,
    val components: List<Component>,
) {
    init {
        require(components.isNotEmpty()) {
            "Module '$name' has no components. " +
                    "This is not allowed, since the module gradle project is implicitly created through its components"
        }
    }
}

internal data class Component(
    val reference: ComponentReference,
    val plugin: Plugin?,
    val dependsOn: Collection<ComponentReference>,
) {
    val name = reference.name
}

data class ComponentReference internal constructor(internal val name: String)

internal data class Plugin(val id: String)
