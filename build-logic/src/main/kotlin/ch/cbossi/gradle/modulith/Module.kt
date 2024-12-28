package ch.cbossi.gradle.modulith

internal sealed class Module {

     constructor(components: List<Component>) {
         require(components.isNotEmpty()) {
             "Module '$name' has no components. " +
                     "This is not allowed, since the module gradle project is implicitly created through its components"
         }
     }

    abstract val components: List<Component>
    abstract val reference: ModuleReference

    val name get() = reference.name

    fun hasComponent(component: Component) = components.map { it.reference }.contains(component.reference)
}

internal data class DomainModule(
    override val reference: ModuleReference,
    override val components: List<Component>,
) : Module(components)

internal data class LibraryModule(
    override val reference: ModuleReference,
    override val components: List<Component>,
) : Module(components)



data class ModuleReference internal constructor(internal val name: String)

internal sealed interface BundleModuleReference
internal data class ChildBundleModuleReference(val name: String) : BundleModuleReference
internal object RootBundleModuleReference : BundleModuleReference

internal data class BundleModule(
    val reference: BundleModuleReference,
    val plugin: Plugin,
    val modules: List<ModuleReference>,
)

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