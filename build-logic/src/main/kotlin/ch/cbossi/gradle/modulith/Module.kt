package ch.cbossi.gradle.modulith

sealed interface Module

internal sealed class ComponentBasedModule : Module {

     constructor(components: List<Component>) {
         require(components.isNotEmpty()) {
             "Module '$name' has no components. " +
                     "This is not allowed, since the module gradle project is implicitly created through its components"
         }
     }

    abstract val reference: ComponentBasedModuleReference
    abstract val components: List<Component>

    val name get() = reference.name

    fun hasComponent(component: Component) = components.map { it.reference }.contains(component.reference)
}

internal data class DomainModule(
    override val reference: DomainModuleReference,
    override val components: List<Component>,
) : ComponentBasedModule(components)

internal data class LibraryModule(
    override val reference: LibraryModuleReference,
    override val components: List<Component>,
) : ComponentBasedModule(components)

internal data class BundleModule(
    val reference: BundleModuleReference,
    val plugin: Plugin,
    val modules: List<ComponentBasedModuleReference>,
) : Module

interface ModuleReference

sealed class ComponentBasedModuleReference : ModuleReference {
    internal abstract val name: String
}

data class DomainModuleReference internal constructor(override val name: String) : ComponentBasedModuleReference()
data class LibraryModuleReference internal constructor(override val name: String) : ComponentBasedModuleReference()

internal sealed interface BundleModuleReference : ModuleReference
internal data class ChildBundleModuleReference(val name: String) : BundleModuleReference
internal object RootBundleModuleReference : BundleModuleReference

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