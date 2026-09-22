package ch.ergon.arciphant

import ch.ergon.arciphant.analyze.registerProjectDependenciesTask
import ch.ergon.arciphant.core.ComponentLayout.PROJECT
import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
import ch.ergon.arciphant.core.FolderCreator
import ch.ergon.arciphant.core.GlobalSettingsRepository
import ch.ergon.arciphant.core.ModuleRepository
import ch.ergon.arciphant.core.project.ProjectLayoutConfigurer
import ch.ergon.arciphant.core.sourceset.SourceSetLayoutConfigurer
import ch.ergon.arciphant.core.toProjectConfigs
import ch.ergon.arciphant.dsl.ArciphantDsl
import ch.ergon.arciphant.sca.componentSourceSetsByProjectPath
import ch.ergon.arciphant.sca.registerValidatePackageStructureTask
import org.gradle.api.initialization.Settings

class ArciphantSettingsPlugin {

    fun apply(settings: Settings) {
        with(settings) {
            val dsl = ArciphantDsl().also { extensions.add(ARCIPHANT_EXTENSION_NAME, it) }

            gradle.settingsEvaluated {
                val settings = GlobalSettingsRepository(dsl).load()
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
                        val configurer = ProjectLayoutConfigurer(settings, projectConfigs)
                        gradle.allprojects {
                            beforeEvaluate { configurer.configure(this) }
                        }
                    }

                    SOURCE_SET -> {
                        val configurer = SourceSetLayoutConfigurer(settings, projectConfigs)
                        gradle.lifecycle.beforeProject {
                            configurer.configure(this)
                        }
                    }
                }

                val componentSourceSetsByProjectPath = projectConfigs.componentSourceSetsByProjectPath(settings)
                gradle.lifecycle.beforeProject {
                    registerValidatePackageStructureTask(
                        packageStructureValidationSettings,
                        componentSourceSetsByProjectPath[path],
                    )
                }
            }

            gradle.projectsLoaded {
                rootProject.registerProjectDependenciesTask()
            }
        }

    }

    companion object {
        private const val ARCIPHANT_EXTENSION_NAME = "arciphant"
    }
}
