package ch.cbossi.gradle.modulith

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

internal class ModuleConfigurer(
    private val configuration: ModulithConfiguration,
    private val module: ComponentBasedModule,
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
        configuration.componentBasedModules.filter { bundle.includes.contains(it.reference) }
            .flatMap { it.componentPaths() }.forEach {
                bundleProject.logger.info("Add bundle dependency: ${bundleProject.path} -> $it")
                bundleProject.dependencies { add("implementation", project(it)) }
            }
    }
}

internal fun ComponentBasedModule.componentPaths() = components.map { ":$name:${it.name}" }