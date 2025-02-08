package ch.ergon.arciphant.core

import ch.ergon.arciphant.model.*
import ch.ergon.arciphant.model.DependencyType.API
import ch.ergon.arciphant.model.DependencyType.IMPLEMENTATION
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

internal sealed class ModuleComposer<M : Module>(
    protected val modules: List<Module>,
    protected val module: M,
    protected val moduleProject: Project,
) {
    abstract fun configure()

    protected fun Project.addDependency(type: DependencyType, dependencyProjectPath: String) {
        logger.info("Add ${type.configurationName} dependency: $path -> $dependencyProjectPath")
        dependencies { add(type.configurationName, project(dependencyProjectPath)) }
    }
}

internal sealed class FunctionalModuleComposer<M : FunctionalModule>(
    modules: List<Module>,
    module: M,
    moduleProject: Project,
) : ModuleComposer<M>(modules, module, moduleProject) {

    override fun configure() {
        module.components.forEach { configure(it) }
    }

    private fun configure(component: Component) {
        val componentProject = moduleProject.childProject(component.reference)
        configure(component, componentProject)
    }

    protected open fun configure(component: Component, componentProject: Project) {
        component.plugin?.let { componentProject.apply(plugin = it.id) }
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

internal class LibraryModuleComposer(modules: List<Module>, module: LibraryModule, moduleProject: Project) :
    FunctionalModuleComposer<LibraryModule>(modules, module, moduleProject)

internal class DomainModuleComposer(modules: List<Module>, module: DomainModule, moduleProject: Project) :
    FunctionalModuleComposer<DomainModule>(modules, module, moduleProject) {

    override fun configure(component: Component, componentProject: Project) {
        super.configure(component, componentProject)
        addDependenciesToLibraries(component, componentProject)
    }

    private fun addDependenciesToLibraries(component: Component, componentProject: Project) {
        libraries.filter { it.hasComponent(component) }.forEach {
            val libraryComponentPath = it.gradleProjectPath(component).value
            componentProject.addDependency(API, libraryComponentPath)
            componentProject.addTestFixturesDependency(libraryComponentPath)
        }
    }

    private val libraries by lazy { modules.filterIsInstance<LibraryModule>() }

}

internal class BundleModuleComposer(
    modules: List<Module>,
    module: BundleModule,
    moduleProject: Project,
) : ModuleComposer<BundleModule>(modules, module, moduleProject) {
    override fun configure() {
        module.plugin?.let { moduleProject.apply(plugin = it.id) }
        modules
            .filter { module.includes.contains(it.reference) }
            .flatMap { it.gradleProjectPaths() }.forEach {
                moduleProject.addDependency(IMPLEMENTATION, it.value)
            }
    }
}

internal fun Module.gradleProjectPaths() = when (this) {
    is FunctionalModule -> components.map { gradleProjectPath(it) }
    is BundleModule -> when (reference) {
        is ChildBundleModuleReference -> listOf(GradleProjectPath(reference.name))
        is RootBundleModuleReference -> listOf(GradleProjectPath())
    }
}

private fun FunctionalModule.gradleProjectPath(component: Component) =
    GradleProjectPath(reference.name, component.reference.name)
