package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.model.*
import ch.ergon.arciphant.core.model.BundleModule
import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.FunctionalModule
import ch.ergon.arciphant.core.model.Module

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

internal fun Module.toProjectConfigs() = when (this) {
    is FunctionalModule -> components.map { GradleComponentProjectConfig(this.gradleProjectPath(it), this, it) }
    is BundleModule -> listOf(GradleBundleModuleProjectConfig(gradleProjectPath(), this))
}

private fun BundleModule.gradleProjectPath() = GradleProjectPath(reference.name)

internal fun FunctionalModule.gradleProjectPath(component: Component) = gradleProjectPath(component.reference)

internal fun FunctionalModule.gradleProjectPath(component: ComponentReference) =
    GradleProjectPath(reference.name, component.name)
