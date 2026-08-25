package ch.ergon.arciphant.core.model

import ch.ergon.arciphant.util.verify

internal sealed interface Module {
    val reference: ModuleReference
}

internal sealed interface FunctionalModule : Module {
    val components: Set<Component>
}

internal data class DomainModule(
    override val reference: ModuleReference,
    override val components: Set<Component>,
) : FunctionalModule

internal data class LibraryModule(
    override val reference: ModuleReference,
    override val components: Set<Component>,
) : FunctionalModule

internal data class BundleModule(
    override val reference: ModuleReference,
    val plugin: Plugin?,
    val includes: Set<ModuleReference>,
) : Module

internal data class Component(
    val reference: ComponentReference,
    val plugin: Plugin?,
    val dependsOn: Set<Dependency>,
    val withTestSourceSet: Boolean?,
    val withTestFixturesSourceSet: Boolean?,
)

internal data class Dependency(val component: ComponentReference, val type: DependencyType)

internal enum class DependencyType(val configurationName: String) {
    IMPLEMENTATION("implementation"),
    API("api")
}

internal data class Plugin(val id: String)

internal fun List<Module>.getByName(moduleName: String): FunctionalModule {
    val module = filterIsInstance<FunctionalModule>().singleOrNull { it.reference.name == moduleName }
    verify(module != null) { "Module with name '$moduleName' does not exist." }
    return module
}

internal fun FunctionalModule.getComponent(componentName: String): Component {
    val component = components.singleOrNull { it.reference.name == componentName }
    verify(component != null) { "Component with name '$componentName' does not exist in module '${this.reference.name}'." }
    return component
}
