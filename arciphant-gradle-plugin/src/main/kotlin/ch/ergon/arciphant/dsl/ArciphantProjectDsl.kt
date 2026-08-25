package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.core.ComponentLayout
import ch.ergon.arciphant.core.ComponentLayout.PROJECT
import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
import ch.ergon.arciphant.core.SourceSetComponentSettings
import ch.ergon.arciphant.core.gradleProjectPath
import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.ComponentReference
import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.model.FunctionalModule
import ch.ergon.arciphant.core.model.Module
import ch.ergon.arciphant.core.sourceset.sourceSetDependencies
import ch.ergon.arciphant.core.sourceset.sourceSets
import ch.ergon.arciphant.util.arciphantPrecondition
import ch.ergon.arciphant.util.verify
import org.gradle.api.Project

open class ArciphantProjectDsl internal constructor(
    private val project: Project,
    private val modules: List<Module>,
    private val componentLayout: ComponentLayout,
    private val componentSettings: SourceSetComponentSettings
) {
    fun api(module: String, component: String) = dependency(API, module, component)

    fun implementation(module: String, component: String) = dependency(IMPLEMENTATION, module, component)

    private fun dependency(type: DependencyType, module: String, component: String) = requireComponentLayout(PROJECT) {
        // TODO implement
    }

    fun component(name: String): ComponentReference = requireComponentLayout(SOURCE_SET) {
        ComponentReference(name)
    }

    fun ComponentReference.api(module: String, component: String) =
        dependency(API, module, component)

    fun ComponentReference.implementation(module: String, component: String) =
        dependency(IMPLEMENTATION, module, component)

    private fun ComponentReference.dependency(type: DependencyType, module: String, component: String) =
        requireComponentLayout(SOURCE_SET) {
            val targetModule = getModule(module)
            val targetComponent = targetModule.getComponent(component)
            val sourceSet = project.sourceSets().findByName(name)
            verify(sourceSet != null) { "Component with name '$name' does not exist in project '${project.path}'." }

            project.sourceSetDependencies(componentSettings) {
                addProjectDependency(
                    type = type,
                    sourceSet = sourceSet,
                    projectPath = targetModule.gradleProjectPath().value,
                    componentName = targetComponent.reference.name,
                    withTestFixturesSourceSet = targetComponent.withTestFixturesSourceSet,
                )
            }
        }

    private fun getModule(moduleName: String): FunctionalModule {
        val module = modules.filterIsInstance<FunctionalModule>().singleOrNull { it.reference.name == moduleName }
        verify(module != null) { "Module with name '$moduleName' does not exist." }
        return module
    }

    private fun FunctionalModule.getComponent(componentName: String): Component {
        val component = components.singleOrNull { it.reference.name == componentName }
        verify(component != null) { "Component with name '$componentName' does not exist in module '${this.reference.name}'." }
        return component
    }

    private fun <R> requireComponentLayout(componentLayout: ComponentLayout, block: () -> R): R {
        arciphantPrecondition(componentLayout == this.componentLayout) {
            "Method ${ArciphantProjectDsl::component.name} is only available in ${ComponentLayout::class.simpleName} $SOURCE_SET"
        }
        return block()
    }
}
