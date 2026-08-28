package ch.ergon.arciphant

import ch.ergon.arciphant.analyze.registerProjectDependenciesTask
import ch.ergon.arciphant.core.ComponentLayout.PROJECT
import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
import ch.ergon.arciphant.core.CoreSettingsRepository
import ch.ergon.arciphant.core.FolderCreator
import ch.ergon.arciphant.core.ModuleRepository
import ch.ergon.arciphant.core.project.ArciphantComponentDsl
import ch.ergon.arciphant.core.project.ProjectLayoutConfigApplicator
import ch.ergon.arciphant.core.sourceset.ArciphantModuleDsl
import ch.ergon.arciphant.core.sourceset.SourceSetLayoutConfigApplicator
import ch.ergon.arciphant.core.toProjectConfigs
import ch.ergon.arciphant.dsl.ArciphantDsl
import ch.ergon.arciphant.sca.registerValidatePackageStructureTask
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging

class ArciphantPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) {
        with(settings) {
            val dsl = extensions.create(ARCIPHANT_EXTENSION_NAME, ArciphantDsl::class.java)

            gradle.settingsEvaluated {
                val settings = CoreSettingsRepository(dsl).load()
                val modules = ModuleRepository(dsl).load()
                val projectConfigs = modules.flatMap { it.toProjectConfigs(settings.componentLayout) }
                val packageStructureValidationSettings = dsl.packageStructureValidation.build()

                // create project folders that do not yet exist
                FolderCreator(settings, rootProject).createFoldersIfNotExists(projectConfigs)

                // create project structure (during gradle initialization phase)
                projectConfigs.map { it.path }.forEach { include(it.value) }

                // apply plugins and add dependencies (during gradle configuration phase)
                when (settings.componentLayout) {
                    PROJECT -> {
                        val configApplicator = ProjectLayoutConfigApplicator(settings, projectConfigs)
                        gradle.allprojects {
                            beforeEvaluate { configApplicator.applyConfig(this) }
                        }
                    }

                    SOURCE_SET -> {
                        val configApplicator = SourceSetLayoutConfigApplicator(settings, projectConfigs)
                        gradle.lifecycle.beforeProject {
                            configApplicator.applyConfig(this)
                        }
                    }
                }

                gradle.lifecycle.beforeProject {
                    when(settings.componentLayout) {
                        PROJECT -> extensions.create(
                            ARCIPHANT_EXTENSION_NAME,
                            ArciphantComponentDsl::class.java,
                            this,
                            modules,
                        )
                        SOURCE_SET -> extensions.create(
                            ARCIPHANT_EXTENSION_NAME,
                            ArciphantModuleDsl::class.java,
                            this,
                            modules,
                            settings.sourceSetComponentSettings
                        )
                    }
                    registerValidatePackageStructureTask(packageStructureValidationSettings)
                }
            }

            gradle.projectsLoaded {
                rootProject.registerProjectDependenciesTask()
            }
        }

    }

    companion object {
        private const val ARCIPHANT_EXTENSION_NAME = "arciphant"

        internal val logger = Logging.getLogger(ArciphantPlugin::class.java)
    }
}
