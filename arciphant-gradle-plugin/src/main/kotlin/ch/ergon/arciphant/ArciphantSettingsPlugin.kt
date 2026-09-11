package ch.ergon.arciphant

import ch.ergon.arciphant.analyze.registerProjectDependenciesTask
import ch.ergon.arciphant.core.ComponentLayout.PROJECT
import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
import ch.ergon.arciphant.core.FolderCreator
import ch.ergon.arciphant.core.GlobalSettingsRepository
import ch.ergon.arciphant.core.ModuleRepository
import ch.ergon.arciphant.core.createComponentDependencyFactory
import ch.ergon.arciphant.core.project.ProjectLayoutConfigApplicator
import ch.ergon.arciphant.core.project.projectComponentDependency
import ch.ergon.arciphant.core.sourceset.SourceSetLayoutConfigApplicator
import ch.ergon.arciphant.core.sourceset.sourceSetComponentDependency
import ch.ergon.arciphant.core.toProjectConfigs
import ch.ergon.arciphant.dsl.ArciphantDsl
import ch.ergon.arciphant.sca.registerValidatePackageStructureTask
import org.gradle.api.initialization.Settings

class ArciphantSettingsPlugin {

    fun apply(settings: Settings) {
        with(settings) {
            val dsl = extensions.create(ARCIPHANT_EXTENSION_NAME, ArciphantDsl::class.java)

            gradle.settingsEvaluated {
                val settings = GlobalSettingsRepository(dsl).load()
                val modules = ModuleRepository(dsl).load()
                val projectConfigs = modules.flatMap { it.toProjectConfigs(settings.componentLayout) }
                val packageStructureValidationSettings = dsl.packageStructureValidation.build()

                // create project folders that do not yet exist
                FolderCreator(settings, rootProject).createFoldersIfNotExists(projectConfigs)

                // create project structure (during gradle initialization phase)
                projectConfigs.map { it.path }.forEach { include(it.value) }

                // registered before the config applicator below: the applicator relies on the extension
                // (it holds the component dependency registry) and can run in the same beforeProject stage
                // when the JVM plugin was already applied to the project (e.g. from an allprojects block)
                gradle.lifecycle.beforeProject {
                    extensions.createComponentDependencyFactory(
                        modules = modules,
                        notation = when (settings.componentLayout) {
                            PROJECT -> projectComponentDependency()
                            SOURCE_SET -> sourceSetComponentDependency()
                        },
                    )
                    registerValidatePackageStructureTask(packageStructureValidationSettings)
                }

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
