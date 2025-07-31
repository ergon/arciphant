package ch.ergon.arciphant.model

internal sealed interface Module {
    val reference: ModuleReference
}

internal sealed interface FunctionalModule : Module {
    override val reference: FunctionalModuleReference
    val components: Set<Component>
}

internal data class DomainModule(
    override val reference: DomainModuleReference,
    override val components: Set<Component>,
) : FunctionalModule

internal data class LibraryModule(
    override val reference: LibraryModuleReference,
    override val components: Set<Component>,
) : FunctionalModule

internal data class BundleModule(
    override val reference: BundleModuleReference,
    val plugin: Plugin?,
    val includes: Set<ModuleReference>,
) : Module

internal data class Component(
    val reference: ComponentReference,
    val plugin: Plugin?,
    val dependsOn: Set<Dependency>,
)

internal data class Dependency(val component: ComponentReference, val type: DependencyType)

internal enum class DependencyType(val configurationName: String) {
    IMPLEMENTATION("implementation"),
    API("api")
}

internal data class Plugin(val id: String)
