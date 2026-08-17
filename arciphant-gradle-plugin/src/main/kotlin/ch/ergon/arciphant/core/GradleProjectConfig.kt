package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.ComponentLayout.PROJECT
import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
import ch.ergon.arciphant.core.model.*

internal sealed interface GradleProjectConfig {
    val path: GradleProjectPath
    val module: Module
}

internal data class GradleBundleModuleProjectConfig(
    override val path: GradleProjectPath,
    override val module: BundleModule
) : GradleProjectConfig

internal data class GradleComponentProjectConfig(
    override val path: GradleProjectPath,
    override val module: FunctionalModule,
    val component: Component
) : GradleProjectConfig

internal data class GradleFunctionalModuleProjectConfig(
    override val path: GradleProjectPath,
    override val module: FunctionalModule,
) : GradleProjectConfig

internal fun Module.toProjectConfigs(componentLayout: ComponentLayout): List<GradleProjectConfig> = when (this) {
    is FunctionalModule -> toProjectConfigs(componentLayout)
    is BundleModule -> listOf(GradleBundleModuleProjectConfig(gradleProjectPath(), this))
}

private fun FunctionalModule.toProjectConfigs(componentLayout: ComponentLayout) = when (componentLayout) {
    PROJECT -> components.map { GradleComponentProjectConfig(this.gradleProjectPath(it), this, it) }
    SOURCE_SET -> listOf(GradleFunctionalModuleProjectConfig(gradleProjectPath(), this))
}

private fun BundleModule.gradleProjectPath() = GradleProjectPath.of(reference.path)

internal fun FunctionalModule.gradleProjectPath(component: Component) = gradleProjectPath(component.reference)

internal fun FunctionalModule.gradleProjectPath(component: ComponentReference) = GradleProjectPath.of(reference.path + component.name)

internal fun FunctionalModule.gradleProjectPath() = GradleProjectPath.of(reference.path)
