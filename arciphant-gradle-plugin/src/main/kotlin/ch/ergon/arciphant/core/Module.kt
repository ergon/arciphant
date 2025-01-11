package ch.ergon.arciphant.core

sealed interface Module {
    val reference: ModuleReference
}

internal sealed class FunctionalModule : Module {

     constructor(components: List<Component>, reference: FunctionalModuleReference) {
         require(components.isNotEmpty()) {
             "Module '${reference.name}' has no components. " +
                     "This is not allowed, since the module gradle project is implicitly created through its components"
         }
     }

    abstract override val reference: FunctionalModuleReference
    abstract val components: List<Component>

    fun hasComponent(component: Component) = components.map { it.reference }.contains(component.reference)
}

internal data class DomainModule(
    override val reference: DomainModuleReference,
    override val components: List<Component>,
) : FunctionalModule(components, reference)

internal data class LibraryModule(
    override val reference: LibraryModuleReference,
    override val components: List<Component>,
) : FunctionalModule(components, reference)

internal data class BundleModule(
    override val reference: BundleModuleReference,
    val plugin: Plugin,
    val includes: List<ModuleReference>,
) : Module

internal sealed interface NamedReference {
    val name: String
}

sealed interface ModuleReference

sealed class FunctionalModuleReference : ModuleReference, NamedReference {
    abstract override val name: String
}

data class DomainModuleReference internal constructor(override val name: String) : FunctionalModuleReference()
data class LibraryModuleReference internal constructor(override val name: String) : FunctionalModuleReference()

internal sealed interface BundleModuleReference : ModuleReference
internal data class ChildBundleModuleReference(override val name: String) : BundleModuleReference, NamedReference
internal object RootBundleModuleReference : BundleModuleReference

internal data class Component(
    val reference: ComponentReference,
    val plugin: Plugin,
    val dependsOn: Collection<Dependency>,
)

data class ComponentReference internal constructor(override val name: String) : NamedReference

internal data class Dependency(val component: ComponentReference, val type: DependencyType)

internal enum class DependencyType(val configurationName: String) {
    IMPLEMENTATION("implementation"),
    API("api")
}

internal data class Plugin(val id: String)
