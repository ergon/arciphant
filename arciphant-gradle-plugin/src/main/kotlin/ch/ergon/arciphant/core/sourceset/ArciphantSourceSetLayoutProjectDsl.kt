package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.SourceSetComponentSettings
import ch.ergon.arciphant.core.gradleProjectPath
import ch.ergon.arciphant.core.model.ComponentReference
import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.model.Module
import ch.ergon.arciphant.core.model.getByName
import ch.ergon.arciphant.core.model.getComponent
import ch.ergon.arciphant.util.verify
import org.gradle.api.Project

open class ArciphantSourceSetLayoutProjectDsl internal constructor(
    private val project: Project,
    private val modules: List<Module>,
    componentSettings: SourceSetComponentSettings
) {

    private val dependencyFactory = SourceSetDependencyFactory(project, componentSettings)

    fun component(name: String) = ComponentReference(name)

    fun ComponentReference.api(module: String, component: String) =
        dependency(API, module, component)

    fun ComponentReference.implementation(module: String, component: String) =
        dependency(IMPLEMENTATION, module, component)

    private fun ComponentReference.dependency(type: DependencyType, module: String, component: String) {
        val targetModule = modules.getByName(module)
        val targetComponent = targetModule.getComponent(component)
        val sourceSet = project.sourceSets().findByName(name)
        verify(sourceSet != null) { "Component with name '$name' does not exist in project '${project.path}'." }

        dependencyFactory.addProjectDependency(
            type = type,
            sourceSet = sourceSet,
            projectPath = targetModule.gradleProjectPath().value,
            componentName = targetComponent.reference.name,
            withTestFixturesSourceSet = targetComponent.withTestFixturesSourceSet,
        )
    }
}
