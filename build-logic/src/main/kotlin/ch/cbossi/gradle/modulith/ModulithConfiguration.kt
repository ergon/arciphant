package ch.cbossi.gradle.modulith

internal data class ModulithConfiguration(
    val modules: List<Module>,
    val bundleModules: List<BundleModule>,
) {
    val libraries by lazy { modules.filter { it.isLibrary } }
}

internal data class Module(
    val reference: ModuleReference,
    val isLibrary: Boolean,
    val components: List<Component>,
) {
    val name = reference.name

    init {
        require(components.isNotEmpty()) {
            "Module '$name' has no components. " +
                    "This is not allowed, since the module gradle project is implicitly created through its components"
        }
    }

    fun hasComponent(component: Component) = components.map { it.reference }.contains(component.reference)
}

data class ModuleReference internal constructor(internal val name: String)

internal sealed interface BundleModule {
    val plugin: Plugin
    val modules: List<ModuleReference>
}

internal data class RootBundleModule(
    override val plugin: Plugin,
    override val modules: List<ModuleReference>,
) : BundleModule

internal data class ChildBundleModule(
    val name: String,
    override val plugin: Plugin,
    override val modules: List<ModuleReference>,
) : BundleModule

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
