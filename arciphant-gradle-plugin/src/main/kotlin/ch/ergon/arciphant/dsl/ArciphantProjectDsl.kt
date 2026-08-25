package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.core.*
import ch.ergon.arciphant.core.model.ComponentReference
import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.model.FunctionalModule
import ch.ergon.arciphant.core.model.Module
import ch.ergon.arciphant.core.sourceset.SourceSetDependencyScope
import ch.ergon.arciphant.core.sourceset.SourceSetFactory
import ch.ergon.arciphant.core.sourceset.sourceSetDependencies
import ch.ergon.arciphant.core.sourceset.sourceSets
import ch.ergon.arciphant.util.verify
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

open class ArciphantProjectDsl internal constructor(
    private val project: Project,
    private val modules: List<Module>,
    private val componentSettings: SourceSetComponentSettings
) {

    fun component(name: String): ComponentReference = ComponentReference(name)

    fun ComponentReference.api(module: String, component: String) = dependency(API, module, component)
    fun ComponentReference.implementation(module: String, component: String) = dependency(IMPLEMENTATION, module, component)

    private fun ComponentReference.dependency(type: DependencyType, module: String, component: String) {
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
}
