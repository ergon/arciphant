package ch.ergon.arciphant.core.project

import ch.ergon.arciphant.core.GradleProjectPath
import ch.ergon.arciphant.core.addMainDependency
import ch.ergon.arciphant.core.addTestFixturesDependency
import ch.ergon.arciphant.core.gradleProjectPath
import ch.ergon.arciphant.core.model.DependencyType
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.model.FunctionalModule
import ch.ergon.arciphant.core.model.Module
import ch.ergon.arciphant.core.model.getByName
import org.gradle.api.Project

open class ArciphantProjectLayoutProjectDsl internal constructor(
    private val project: Project,
    private val modules: List<Module>,
) {

    fun api(module: String, component: String) = dependency(API, module, component)

    fun implementation(module: String, component: String) = dependency(IMPLEMENTATION, module, component)

    private fun dependency(type: DependencyType, module: String, component: String) {
        val targetPath = modules.getByName(module).gradleProjectPath(component)
        project.addMainDependency(type, targetPath)
        project.addTestFixturesDependency(targetPath)
    }

    private fun FunctionalModule.gradleProjectPath(component: String) =
        GradleProjectPath.of(gradleProjectPath().projectNames + component)

}