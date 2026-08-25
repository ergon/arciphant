package ch.ergon.arciphant.core.project

import ch.ergon.arciphant.core.addMainDependency
import ch.ergon.arciphant.core.addTestFixturesDependency
import ch.ergon.arciphant.core.gradleProjectPath
import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.model.Module
import ch.ergon.arciphant.core.model.getByName
import ch.ergon.arciphant.core.model.getComponent
import org.gradle.api.Project

open class ArciphantProjectLayoutProjectDsl internal constructor(
    private val project: Project,
    private val modules: List<Module>,
) {

    fun api(module: String, component: String) = dependency(API, module, component)

    fun implementation(module: String, component: String) = dependency(IMPLEMENTATION, module, component)

    private fun dependency(type: DependencyType, module: String, component: String) {
        val targetModule = modules.getByName(module)
        val targetComponent = targetModule.getComponent(component)
        val targetPath = targetModule.gradleProjectPath(targetComponent.reference)
        project.addMainDependency(type, targetPath)
        project.addTestFixturesDependency(type, targetPath)
    }

}