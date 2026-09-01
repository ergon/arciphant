package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.gradleProjectPath
import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.Module
import ch.ergon.arciphant.core.model.getByName
import ch.ergon.arciphant.core.model.getComponent
import ch.ergon.arciphant.core.sourceset.ComponentDependencyFactory.Companion.COMPONENT_EXTENSION_NAME
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.ExtensionContainer
import java.util.IdentityHashMap

/**
 * Registered as the `component` extension in every project of the
 * [ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET] layout. Its invoke operator creates dependency
 * notations for components of other modules, in the same style as external dependencies:
 *
 * ```
 * dependencies {
 *     "domainApi"(component(module = "exam", component = "api"))
 * }
 * ```
 *
 * The notation is a project dependency on the target component's `…ApiElements` configuration; Arciphant
 * resolves the target Gradle project path from the module configuration. The matching runtime dependency
 * and the test fixtures mirroring are added automatically (see [InterModuleDependencyMirror]).
 */
open class ComponentDependencyFactory internal constructor(
    private val project: Project,
    private val modules: List<Module>,
    internal val registry: ComponentDependencyRegistry,
) {

    operator fun invoke(module: String, component: String): ProjectDependency {
        val targetModule = modules.getByName(module)
        val targetComponent = targetModule.getComponent(component)
        val dependency = project.projectDependency(
            targetModule.gradleProjectPath().value,
            targetComponent.reference.name.apiElementsConfigurationName(),
        )
        registry.register(dependency, targetComponent)
        return dependency
    }

    companion object {
        internal const val COMPONENT_EXTENSION_NAME = "component"
    }
}

/**
 * Identifies the project dependencies created by Arciphant's component notations, by instance identity.
 * The [InterModuleDependencyMirror] only completes dependencies known to this registry — a hand-written
 * `project(path, configuration)` dependency is left untouched. The registry also carries the resolved
 * target [Component] (including its test fixtures availability), so the mirror needs no access to the
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
    project: Project,
    modules: List<Module>,
) {
    create(
        COMPONENT_EXTENSION_NAME,
        ComponentDependencyFactory::class.java,
        project,
        modules,
        ComponentDependencyRegistry(),
    )
}
