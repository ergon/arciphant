package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.model.Module
import org.gradle.api.Project

/**
 * Base of the layout-specific config applicators, which apply the configuration derived from the Arciphant
 * settings to the Gradle projects. Projects that are not managed by Arciphant are ignored.
 *
 * For every managed project, the applicator first registers the `component` dependency notation extension,
 * parameterized with the layout-specific [ComponentDependencyNotation]. Registering it here (instead of in
 * a separate callback) guarantees that the extension exists before anything that relies on it runs: the
 * layout-specific configuration itself (which uses the extension's [ComponentDependencyRegistry]) and the
 * build scripts (including their Kotlin DSL accessor generation). Deliberately, projects where the notation
 * is not supported do not get the extension — using it there would not be completed, so it fails at compile
 * time instead: this applies to unmanaged projects and to bundle projects, which bundle whole modules and
 * must not depend on individual components.
 */
internal abstract class LayoutConfigApplicator(projectConfigs: List<GradleProjectConfig>) {

    // in the project layout, a module is referenced by several project configs (one per component)
    private val modules: List<Module> = projectConfigs.map { it.module }.distinct()

    private val projectConfigsByPath = projectConfigs.associateBy { it.path.value }

    fun applyConfig(project: Project) {
        val config = projectConfigsByPath[project.path] ?: return

        if (config !is GradleBundleModuleProjectConfig) {
            project.extensions.createComponentDependencyFactory(
                modules = modules,
                notation = componentDependencyNotation(project),
            )
        }
        doApplyConfig(project, config)
    }

    protected abstract fun componentDependencyNotation(project: Project): ComponentDependencyNotation

    protected abstract fun doApplyConfig(project: Project, config: GradleProjectConfig)
}
