package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.SourceSetComponentSettings
import ch.ergon.arciphant.core.gradleProjectPath
import ch.ergon.arciphant.core.model.*
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.sourceset.ArciphantModuleDsl.Companion.ARCIPHANT_MODULE_EXTENSION_NAME
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.SourceSet

/**
 * The [ArciphantModuleDsl] is used together with [ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET].
 */
open class ArciphantModuleDsl internal constructor(
    private val project: Project,
    private val modules: List<Module>,
    componentSettings: SourceSetComponentSettings,
) {

    private val dependencyFactory = SourceSetDependencyFactory(project, componentSettings)

    /**
     * References a component of another module, for use as a dependency notation:
     * `"domainApi"(component(module = "exam", component = "api"))`.
     */
    fun component(module: String, component: String): ComponentDependency {
        val targetModule = modules.getByName(module)
        return ComponentDependency(targetModule, targetModule.getComponent(component))
    }

    /**
     * Adds a dependency on a component of another module to this dependency configuration — in the same
     * style as external dependencies are declared in the `dependencies` block: the configuration name
     * determines the source set and the dependency type (e.g. `"domainApi"` or `"domainImplementation"`).
     * The matching runtime dependency is added automatically, and if both the source and the target
     * component have a test fixtures source set, the dependency between the test fixtures source sets is
     * added as well.
     */
    operator fun String.invoke(dependency: ComponentDependency) {
        val (sourceSet, type) = dependencyConfiguration(this)
        dependencyFactory.addInterModuleDependency(
            type = type,
            sourceSet = sourceSet,
            projectPath = dependency.module.gradleProjectPath().value,
            componentName = dependency.component.reference.name,
            withTestFixturesSourceSet = dependency.component.withTestFixturesSourceSet,
        )
    }

    private fun dependencyConfiguration(configurationName: String): Pair<SourceSet, DependencyType> {
        project.sourceSets().forEach { sourceSet ->
            when (configurationName) {
                sourceSet.apiConfigurationName -> return sourceSet to API
                sourceSet.implementationConfigurationName -> return sourceSet to IMPLEMENTATION
            }
        }
        throw IllegalArgumentException(
            "Arciphant configuration error: Configuration '$configurationName' is not an api or implementation configuration of a source set in project '${project.path}'."
        )
    }

    companion object {
        internal const val ARCIPHANT_MODULE_EXTENSION_NAME = "arciphantModule"
    }
}

/**
 * A dependency notation referencing a component of another module, created by [ArciphantModuleDsl.component].
 */
class ComponentDependency internal constructor(
    internal val module: FunctionalModule,
    internal val component: Component,
)

/**
 * Keep parameters of this method in sync with constructor of [ArciphantModuleDsl].
 */
internal fun ExtensionContainer.createArciphantModuleDsl(
    project: Project,
    modules: List<Module>,
    componentSettings: SourceSetComponentSettings
) {
    create(
        ARCIPHANT_MODULE_EXTENSION_NAME,
        ArciphantModuleDsl::class.java,
        project,
        modules,
        componentSettings,
    )
}
