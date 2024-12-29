package ch.cbossi.gradle.modulith

import ch.cbossi.gradle.modulith.DependencyType.API
import ch.cbossi.gradle.modulith.DependencyType.IMPLEMENTATION
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

internal sealed class ModuleComposer<M : Module>(
    protected val configuration: ModuleStructure,
    protected val module: M,
    protected val moduleProject: Project,
) {
    abstract fun configure()

    protected fun Project.addDependency(type: DependencyType, dependencyProjectPath: String) {
        logger.info("Add ${type.configurationName} dependency: $path -> $dependencyProjectPath")
        dependencies { add(type.configurationName, project(dependencyProjectPath)) }
    }
}

internal sealed class ComponentBasedModuleComposer<M : ComponentBasedModule>(
    configuration: ModuleStructure,
    module: M,
    moduleProject: Project,
) : ModuleComposer<M>(configuration, module, moduleProject) {

    override fun configure() {
        module.components.forEach { configure(it) }
    }

    private fun configure(component: Component) {
        val componentProject = moduleProject.childProject(component.reference)
        configure(component, componentProject)
    }

    protected open fun configure(component: Component, componentProject: Project) {
        componentProject.apply(plugin = component.plugin.id)
        component.dependsOn.forEach {
            val dependencyProject = moduleProject.childProject(it.component)
            componentProject.addDependency(it.type, dependencyProject.path)
            componentProject.addTestFixturesDependency(dependencyProject.path)
        }
    }

    protected fun Project.addTestFixturesDependency(dependencyProjectPath: String) {
        pluginManager.withPlugin("java-test-fixtures") {
            dependencies { add("testFixturesApi", testFixtures(project(dependencyProjectPath))) }
        }
    }
}

internal class LibraryModuleComposer(configuration: ModuleStructure, module: LibraryModule, moduleProject: Project) :
    ComponentBasedModuleComposer<LibraryModule>(configuration, module, moduleProject)

internal class DomainModuleComposer(configuration: ModuleStructure, module: DomainModule, moduleProject: Project) :
    ComponentBasedModuleComposer<DomainModule>(configuration, module, moduleProject) {

    override fun configure(component: Component, componentProject: Project) {
        super.configure(component, componentProject)
        addDependenciesToLibraries(component, componentProject)
    }

    private fun addDependenciesToLibraries(component: Component, componentProject: Project) {
        configuration.libraries.filter { it.hasComponent(component) }.forEach {
            val libraryComponentPath = it.componentPath(component)
            componentProject.addDependency(API, libraryComponentPath)
            componentProject.addTestFixturesDependency(libraryComponentPath)
        }
    }

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
                moduleProject.addDependency(IMPLEMENTATION, it)
            }
    }
}

internal fun ComponentBasedModule.componentPaths() = components.map { componentPath(it) }

internal fun ComponentBasedModule.componentPath(component: Component) = ":${reference.name}:${component.reference.name}"