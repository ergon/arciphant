package ch.ergon.arciphant.core

import ch.ergon.arciphant.model.*

internal sealed interface GradleProjectConfig {
    val path: GradleProjectPath
}

internal data class GradleModuleProjectConfig(
    override val path: GradleProjectPath,
    val module: Module
): GradleProjectConfig

internal data class GradleComponentProjectConfig(
    override val path: GradleProjectPath,
    val parentModule: FunctionalModule,
    val component: Component
): GradleProjectConfig

internal fun Module.toProjectConfigs() = when (this) {
    is FunctionalModule -> components.map { GradleComponentProjectConfig(this.gradleProjectPath(it), this, it) }
    is BundleModule -> listOf(GradleModuleProjectConfig(path(), this))
}

private fun BundleModule.path() = when (reference) {
    is ChildBundleModuleReference -> GradleProjectPath(reference.name)
    is RootBundleModuleReference -> GradleProjectPath()
}
