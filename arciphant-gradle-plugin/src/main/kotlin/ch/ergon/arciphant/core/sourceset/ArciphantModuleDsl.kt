package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.SourceSetComponentSettings
import ch.ergon.arciphant.core.gradleProjectPath
import ch.ergon.arciphant.core.model.*
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.sourceset.ArciphantModuleDsl.Companion.ARCIPHANT_MODULE_EXTENSION_NAME
import ch.ergon.arciphant.util.verify
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.ExtensionContainer

/**
 * The [ArciphantModuleDsl] is used together with [ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET].
 */
open class ArciphantModuleDsl internal constructor(
    private val project: Project,
    private val modules: List<Module>,
    componentSettings: SourceSetComponentSettings,
) {

    private val dependencyFactory = SourceSetDependencyFactory(project, componentSettings)

    fun component(name: String) = ComponentReference(name)

    /**
     * Creates a dependency notation for the given component of another module, for use in a `dependencies`
     * block: `"domainApi"(arciphantModule.component(module = "exam", component = "api"))`. Arciphant
     * automatically adds the runtime dependency and mirrors the dependency between the test fixtures source
     * sets if both the source and the target component have one (see [InterModuleDependencyMirror]).
     */
    fun component(module: String, component: String): ProjectDependency {
        val targetModule = modules.getByName(module)
        val targetComponent = targetModule.getComponent(component)
        return project.projectDependency(
            targetModule.gradleProjectPath().value,
            targetComponent.reference.name.apiElementsConfigurationName(),
        )
    }

    fun ComponentReference.api(module: String, component: String) =
        dependency(API, module, component)

    fun ComponentReference.implementation(module: String, component: String) =
        dependency(IMPLEMENTATION, module, component)

    private fun ComponentReference.dependency(type: DependencyType, module: String, component: String) {
        val targetModule = modules.getByName(module)
        val targetComponent = targetModule.getComponent(component)
        val sourceSet = project.sourceSets().findByName(name)
        verify(sourceSet != null) { "Component with name '$name' does not exist in project '${project.path}'." }

        dependencyFactory.addInterModuleDependency(
            type = type,
            sourceSet = sourceSet,
            projectPath = targetModule.gradleProjectPath().value,
            componentName = targetComponent.reference.name,
        )
    }

    companion object {
        internal const val ARCIPHANT_MODULE_EXTENSION_NAME = "arciphantModule"
    }
}

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
