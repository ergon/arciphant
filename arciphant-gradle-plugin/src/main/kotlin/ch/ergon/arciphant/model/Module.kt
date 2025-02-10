package ch.ergon.arciphant.model

internal sealed interface Module {
    val reference: ModuleReference
}

internal sealed interface FunctionalModule : Module {
    override val reference: FunctionalModuleReference
    val components: List<Component>
}

internal data class DomainModule(
    override val reference: DomainModuleReference,
    override val components: List<Component>,
) : FunctionalModule

internal data class LibraryModule(
    override val reference: LibraryModuleReference,
    override val components: List<Component>,
) : FunctionalModule {

    fun hasComponent(component: Component) = components.map { it.reference }.contains(component.reference)
}

internal data class BundleModule(
    override val reference: BundleModuleReference,
    val plugin: Plugin?,
    val includes: List<ModuleReference>,
) : Module

internal data class Component(
    val reference: ComponentReference,
    val plugin: Plugin?,
    val dependsOn: Collection<Dependency>,
)

internal data class Dependency(val component: ComponentReference, val type: DependencyType)

internal enum class DependencyType(val configurationName: String) {
    IMPLEMENTATION("implementation"),
    API("api")
}

internal data class Plugin(val id: String)
