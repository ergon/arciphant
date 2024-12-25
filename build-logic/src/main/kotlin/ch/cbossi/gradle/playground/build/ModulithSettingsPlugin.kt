package ch.cbossi.gradle.playground.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.apply

class ModulithSettingsPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        with(settings) {
            val extension = extensions.getByType(ModulithExtension::class.java)
            val modules = extension.getConfiguration().modules
            configure(modules)
            gradle.projectsLoaded {
                gradle.rootProject.configure(modules)
            }
        }

    }

    private fun Settings.configure(modules: Collection<Module>) = modules.forEach { configure(it) }

    private fun Settings.configure(module: Module) {
        module.components.forEach { include("${module.name}:${it.name}") }
    }

    private fun Project.configure(modules: Collection<Module>) {
        modules.forEach { it.configure(gradleChildProject(it.name)) }
    }

    private fun Module.configure(moduleProject: Project) {
        components.forEach { it.configureComponent(moduleProject) }
    }

    private fun Component.configureComponent(moduleProject: Project) {
        val componentProject = moduleProject.gradleChildProject(name)
        componentProject.apply(plugin = plugin.id)
        dependsOn.forEach {
            val dependencyProject = moduleProject.gradleChildProject(it.component.name)
            componentProject.logger.info("Add component dependency: ${it.type.configurationName} ${componentProject.path} -> ${dependencyProject.path}")
            componentProject.dependencies.add(it.type.configurationName, dependencyProject)
        }
    }
}

private fun Project.gradleChildProject(name: String) = childProjects.getValue(name)
