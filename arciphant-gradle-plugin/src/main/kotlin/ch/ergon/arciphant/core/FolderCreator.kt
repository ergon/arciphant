package ch.ergon.arciphant.core

import ch.ergon.arciphant.ArciphantPlugin
import ch.ergon.arciphant.ArciphantPlugin.Companion.logger
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
        rootProject.projectDir.resolve(projectConfig.folderPath)
            .createDirectoryIfNotExists()
    }

    private val GradleProjectConfig.folderPath get() = path.folderPath

    private val GradleProjectPath.folderPath get() = projectNames.joinToString("/")

    private fun File.createDirectoryIfNotExists() {
        if (!exists()) {
            mkdirs()
            logger.info("Created project folder $absolutePath")
        }
    }
}
