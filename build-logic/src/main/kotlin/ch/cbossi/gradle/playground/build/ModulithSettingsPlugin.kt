package ch.cbossi.gradle.playground.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class ModulithSettingsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        with(settings) {
            val extension = extensions.getByType(ModulithExtension::class.java)
            val configuration = extension.getConfiguration()
            configuration.createProjectStructure().forEach { include(it) }
            gradle.projectsLoaded {
                configuration.configureModules(gradle.rootProject)
            }
        }

    }
}

private fun ModulithConfiguration.createProjectStructure() =
    modules.flatMap { module -> module.components.map { "${module.name}:${it.name}" } }

private fun ModulithConfiguration.configureModules(rootProject: Project) {
    modules.forEach { ModuleConfigurer(it, rootProject.childProject(it.name)).configure() }
}

internal class ModuleConfigurer(
    private val module: Module,
    private val moduleProject: Project,
) {

    fun configure() {
        module.components.forEach { it.configureComponent() }
    }

    private fun Component.configureComponent() {
        val componentProject = moduleProject.childProject(name)
        componentProject.apply(plugin = plugin.id)
        dependsOn.forEach {
            val dependencyProject = moduleProject.childProject(it.component.name)
            componentProject.logger.info("Add component dependency: ${it.type.configurationName} ${componentProject.path} -> ${dependencyProject.path}")
            componentProject.dependencies.add(it.type.configurationName, dependencyProject)
            componentProject.pluginManager.withPlugin("java-test-fixtures") {
                componentProject.dependencies { add("testFixturesApi", testFixtures(project(dependencyProject.path))) }
            }
        }
    }

}

private fun Project.childProject(name: String) = childProjects.getValue(name)