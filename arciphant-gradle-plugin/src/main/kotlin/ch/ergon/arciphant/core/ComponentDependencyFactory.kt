package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.ComponentDependencyFactory.Companion.COMPONENT_EXTENSION_NAME
import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.FunctionalModule
import ch.ergon.arciphant.core.model.Module
import ch.ergon.arciphant.core.model.getByName
import ch.ergon.arciphant.core.model.getComponent
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.ExtensionContainer
import java.util.IdentityHashMap

/**
 * Registered as the `component` extension in every project. Its invoke operator creates dependency
 * notations for components of other modules, in the same style as external dependencies:
 *
 * ```
 * dependencies {
 *     "domainApi"(component(module = "exam", component = "api"))
 * }
 * ```
 *
 * Arciphant resolves the target from the module configuration through the layout-specific
 * [ComponentDependencyNotation] and completes the dependency automatically (runtime and test fixtures
 * legs in the source set layout, the test fixtures dependency in the project layout) when it is added
 * to a supported configuration.
 */
open class ComponentDependencyFactory internal constructor(
    private val modules: List<Module>,
    internal val registry: ComponentDependencyRegistry,
    private val notation: ComponentDependencyNotation,
) {

    operator fun invoke(module: String, component: String): ProjectDependency {
        val targetModule = modules.getByName(module)
        val targetComponent = targetModule.getComponent(component)
        val dependency = notation.create(targetModule, targetComponent)
        registry.register(dependency, targetComponent)
        return dependency
    }

    companion object {
        internal const val COMPONENT_EXTENSION_NAME = "component"
    }
}

/**
 * Creates the layout-specific project dependency for a component of another module.
 */
internal fun interface ComponentDependencyNotation {
    fun create(module: FunctionalModule, component: Component): ProjectDependency
}

/**
 * Identifies the project dependencies created by Arciphant's component notations, by instance identity.
 * The completion mechanisms only act on dependencies known to this registry — a hand-written
 * `project(...)` dependency is left untouched. The registry also carries the resolved target
 * [Component] (including its test fixtures availability), so the completion needs no access to the
 * module model.
 */
internal class ComponentDependencyRegistry {

    private val components = IdentityHashMap<ProjectDependency, Component>()

    fun register(dependency: ProjectDependency, component: Component) {
        components[dependency] = component
    }

    fun findComponent(dependency: ProjectDependency): Component? = components[dependency]
}

internal fun Project.componentDependencyRegistry(): ComponentDependencyRegistry =
    extensions.getByType(ComponentDependencyFactory::class.java).registry

/**
 * Keep parameters of this method in sync with constructor of [ComponentDependencyFactory].
 */
internal fun ExtensionContainer.createComponentDependencyFactory(
    modules: List<Module>,
    notation: ComponentDependencyNotation,
) {
    create(
        COMPONENT_EXTENSION_NAME,
        ComponentDependencyFactory::class.java,
        modules,
        ComponentDependencyRegistry(),
        notation,
    )
}
