package ch.cbossi.gradle.modulith

import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

sealed class ModuleComposer<M : Module>(
    internal val configuration: ModuleStructure,
    internal val module: M,
    internal val moduleProject: Project,
) {
    abstract fun configure()
}

internal sealed class ComponentBasedModuleComposer<M : ComponentBasedModule>(
    configuration: ModuleStructure,
    module: M,
    moduleProject: Project,
) : ModuleComposer<M>(configuration, module, moduleProject) {

    override fun configure() {
        module.components.forEach { it.configureComponent() }
    }

    private fun Component.configureComponent() {
        val componentProject = moduleProject.childProject(reference)
        componentProject.apply(plugin = plugin.id)
        dependsOn.forEach {
            val dependencyProject = moduleProject.childProject(it.component)
            componentProject.logger.info("Add component dependency: ${it.type.configurationName} ${componentProject.path} -> ${dependencyProject.path}")
            componentProject.dependencies.add(it.type.configurationName, dependencyProject)
            componentProject.pluginManager.withPlugin("java-test-fixtures") {
                componentProject.dependencies { add("testFixturesApi", testFixtures(project(dependencyProject.path))) }
            }
        }
        if (module is DomainModule) {
            configuration.libraries.filter { it.hasComponent(this) }.forEach {
                val libraryComponentPath = it.componentPath(this)
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

internal class LibraryModuleComposer(
    configuration: ModuleStructure,
    module: LibraryModule,
    moduleProject: Project,
) : ComponentBasedModuleComposer<LibraryModule>(configuration, module, moduleProject) {

}

internal class DomainModuleComposer(
    configuration: ModuleStructure,
    module: DomainModule,
    moduleProject: Project,
) : ComponentBasedModuleComposer<DomainModule>(configuration, module, moduleProject) {

}

internal class BundleModuleComposer(
    configuration: ModuleStructure,
    module: BundleModule,
    moduleProject: Project,
) : ModuleComposer<BundleModule>(configuration, module, moduleProject) {
    override fun configure() {
        moduleProject.apply(plugin = module.plugin.id)
        configuration.modules
            .filterIsInstance<ComponentBasedModule>()
            .filter { module.includes.contains(it.reference) }
            .flatMap { it.componentPaths() }.forEach {
                moduleProject.logger.info("Add bundle dependency: ${moduleProject.path} -> $it")
                moduleProject.dependencies { add("implementation", project(it)) }
            }
    }
}

internal fun ComponentBasedModule.componentPaths() = components.map { componentPath(it) }

internal fun ComponentBasedModule.componentPath(component: Component) = ":${reference.name}:${component.reference.name}"