package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.core.ComponentLayout
import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
import ch.ergon.arciphant.core.SourceSetComponentSettings
import ch.ergon.arciphant.core.gradleProjectPath
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

    fun component(name: String): ComponentReference = requireComponentLayout(SOURCE_SET) {
        ComponentReference(name)
    }

    fun ComponentReference.api(module: String, component: String) =
        dependency(API, module, component)

    fun ComponentReference.implementation(module: String, component: String) =
        dependency(IMPLEMENTATION, module, component)

    private fun ComponentReference.dependency(type: DependencyType, module: String, component: String) =
        requireComponentLayout(SOURCE_SET) {
            val targetModule = modules.filterIsInstance<FunctionalModule>().singleOrNull { it.reference.name == module }
            verify(targetModule != null) { "Module with name '$module' does not exist." }
            val targetComponent = targetModule.components.singleOrNull { it.reference.name == component }
            verify(targetComponent != null) { "Component with name '$component' does not exist in module '$module'." }
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

    private fun <R> requireComponentLayout(componentLayout: ComponentLayout, block: () -> R): R {
        arciphantPrecondition(componentLayout == this.componentLayout) {
            "Method ${ArciphantProjectDsl::component.name} is only available in ${ComponentLayout::class.simpleName} $SOURCE_SET"
        }
        return block()
    }
}
