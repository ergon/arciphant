package ch.cbossi.gradle.modulith

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
    modules.flatMap { it.componentPaths() } +
            bundleModules.map { it.reference }.filterIsInstance<ChildBundleModuleReference>().map { it.name }

private fun ModulithConfiguration.configureModules(rootProject: Project) {
    modules.forEach { ModuleConfigurer(this, it, rootProject.childProject(it.name)).configure() }
    bundleModules.forEach { BundleModuleConfigurer(this, it, it.project(rootProject)).configure() }
}

private fun BundleModule.project(rootProject: Project) = when (this.reference) {
    is RootBundleModuleReference -> rootProject
    is ChildBundleModuleReference -> rootProject.childProject(this.reference.name)
}

internal class ModuleConfigurer(
    private val configuration: ModulithConfiguration,
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
        if (module is DomainModule) {
            configuration.libraries.filter { it.hasComponent(this) }.forEach {
                val libraryComponentPath = ":${it.name}:$name"
                componentProject.logger.info("Add library dependency: ${componentProject.path} -> $libraryComponentPath")
                componentProject.dependencies { add("api", project(libraryComponentPath)) }
                componentProject.pluginManager.withPlugin("java-test-fixtures") {
                    componentProject.dependencies {
                        add(
                            "testFixturesApi",
                            testFixtures(project(libraryComponentPath))
                        )
                    }
                }
            }
        }
    }
}

internal class BundleModuleConfigurer(
    private val configuration: ModulithConfiguration,
    private val bundle: BundleModule,
    private val bundleProject: Project,
) {
    fun configure() {
        bundleProject.apply(plugin = bundle.plugin.id)
        configuration.modules.filter { bundle.modules.contains(it.reference) }.flatMap { it.componentPaths() }.forEach {
            bundleProject.logger.info("Add bundle dependency: ${bundleProject.path} -> $it")
            bundleProject.dependencies { add("implementation", project(it)) }
        }
    }
}

private fun Project.childProject(name: String) = childProjects.getValue(name)

private fun Module.componentPaths() = components.map { ":$name:${it.name}" }