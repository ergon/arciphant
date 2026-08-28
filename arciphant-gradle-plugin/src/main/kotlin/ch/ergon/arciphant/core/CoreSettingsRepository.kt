package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.ComponentLayout.PROJECT
import ch.ergon.arciphant.core.ComponentLayout.SOURCE_SET
import ch.ergon.arciphant.core.SourceSetComponentSettings.Companion.DEFAULT_SETTINGS
import ch.ergon.arciphant.dsl.ArciphantDsl

internal class CoreSettingsRepository(private val dsl: ArciphantDsl) {

    fun load(): CoreSettings {
        validateComponentLayoutSpecificSettings()
        return CoreSettings(
            disableFolderCreation = dsl.disableFolderCreation,
            componentLayout = dsl.componentLayout,
            projectComponentSettings = ProjectComponentSettings(
                disableQualifiedArchiveBaseName = dsl.disableQualifiedArchiveBaseName ?: false,
            ),
            sourceSetComponentSettings = SourceSetComponentSettings(
                withTestSourceSet = dsl.withTestSourceSet ?: DEFAULT_SETTINGS.withTestSourceSet,
                withTestFixturesSourceSet = dsl.withTestFixturesSourceSet ?: DEFAULT_SETTINGS.withTestFixturesSourceSet,
                testSourceSetName = dsl.testSourceSetName ?: DEFAULT_SETTINGS.testSourceSetName,
                testFixturesSourceSetName = dsl.testFixturesSourceSetName ?: DEFAULT_SETTINGS.testFixturesSourceSetName,
            ),
        )
    }

    private fun validateComponentLayoutSpecificSettings() {
        when (dsl.componentLayout) {
            PROJECT -> {
                PROJECT.verifyConfig(dsl.withTestSourceSet, dsl::withTestSourceSet)
                PROJECT.verifyConfig(dsl.withTestFixturesSourceSet, dsl::withTestFixturesSourceSet)
                PROJECT.verifyConfig(dsl.testSourceSetName, dsl::testSourceSetName)
                PROJECT.verifyConfig(dsl.testFixturesSourceSetName, dsl::testFixturesSourceSetName)
            }

            SOURCE_SET -> {
                SOURCE_SET.verifyConfig(dsl.disableQualifiedArchiveBaseName, dsl::disableQualifiedArchiveBaseName)
            }
        }
    }

}
