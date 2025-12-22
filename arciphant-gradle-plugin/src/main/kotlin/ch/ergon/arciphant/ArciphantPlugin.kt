package ch.ergon.arciphant

import ch.ergon.arciphant.analyze.registerProjectDependenciesTask
import ch.ergon.arciphant.core.CoreSettingsRepository
import ch.ergon.arciphant.core.FolderCreator
import ch.ergon.arciphant.core.GradleProjectConfigApplicator
import ch.ergon.arciphant.core.ModuleRepository
import ch.ergon.arciphant.core.toProjectConfigs
import ch.ergon.arciphant.dsl.ArciphantDsl
import ch.ergon.arciphant.sca.PackageStructureValidator
import ch.ergon.arciphant.sca.registerValidatePackageStructureTask
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging

class ArciphantPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        with(settings) {
            val dsl = extensions.create("arciphant", ArciphantDsl::class.java)

            gradle.settingsEvaluated {
                val settings = CoreSettingsRepository(dsl).load()
                val modules = ModuleRepository(dsl).load()
                val projectConfigs = modules.flatMap { it.toProjectConfigs() }

                // create project folders that do not yet exist
                FolderCreator(rootProject).createFoldersIfNotExists(projectConfigs)

                // create project structure (during gradle initialization phase)
                projectConfigs.map { it.path }.forEach { include(it.value) }

                // apply plugins and add dependencies (during gradle configuration phase)
                val configApplicator = GradleProjectConfigApplicator(settings, projectConfigs)
                gradle.allprojects {
                    beforeEvaluate { configApplicator.applyConfig(this) }
                }
            }

            gradle.projectsLoaded {
                val packageStructureValidator = PackageStructureValidator(dsl.packageStructureValidation.build())
                rootProject.registerValidatePackageStructureTask(packageStructureValidator)
                rootProject.registerProjectDependenciesTask()
            }
        }

    }

    companion object {
        internal val logger = Logging.getLogger(ArciphantPlugin::class.java)
    }
}
