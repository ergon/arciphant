package ch.ergon.arciphant.core

import ch.ergon.arciphant.ArciphantPlugin.Companion.logger
import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
import ch.ergon.arciphant.core.model.Component
import org.gradle.api.initialization.ProjectDescriptor
import java.io.File

internal class FolderCreator(
    private val settings: CoreSettings,
    private val rootProject: ProjectDescriptor
) {

    fun createFoldersIfNotExists(projectConfigs: List<GradleProjectConfig>) {
        if (!settings.disableFolderCreation) {
            projectConfigs.forEach { createFoldersIfNotExists(it) }
        }
    }

    private fun createFoldersIfNotExists(projectConfig: GradleProjectConfig) {
        val projectFolder = rootProject.projectDir.resolve(projectConfig.folderPath)
        projectFolder.createDirectoryIfNotExists()
        if (settings.componentLayout == SOURCE_SET && projectConfig is GradleFunctionalModuleProjectConfig) {
            projectConfig.module.components.forEach { projectFolder.createSourceSetFoldersIfNotExists(it) }
        }
    }

    private fun File.createSourceSetFoldersIfNotExists(component: Component) {
        component.sourceSetNames().forEach {
            resolve("src").resolve(it).createDirectoryIfNotExists()
        }
    }

    private fun Component.sourceSetNames(): List<String> {
        val sourceSetSettings = settings.sourceSetComponentSettings
        val name = reference.name
        return listOfNotNull(
            name,
            sourceSetSettings.testSourceSetName(name)
                .takeIf { withTestSourceSet ?: sourceSetSettings.withTestSourceSet },
            sourceSetSettings.testFixturesSourceSetName(name)
                .takeIf { withTestFixturesSourceSet ?: sourceSetSettings.withTestFixturesSourceSet },
        )
    }

    private val GradleProjectConfig.folderPath get() = path.folderPath

    private val GradleProjectPath.folderPath get() = projectNames.joinToString("/")

    private fun File.createDirectoryIfNotExists() {
        if (!exists()) {
            mkdirs()
            logger.info("Created folder $absolutePath")
        }
    }
}
