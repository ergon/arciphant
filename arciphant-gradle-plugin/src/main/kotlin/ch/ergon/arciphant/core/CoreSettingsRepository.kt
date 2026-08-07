package ch.ergon.arciphant.core

import ch.ergon.arciphant.dsl.ArciphantSettingsDsl

internal class CoreSettingsRepository(private val dsl: ArciphantSettingsDsl) {

    fun load() = CoreSettings(
        disableFolderCreation = dsl.disableFolderCreation,
        disableQualifiedArchiveBaseName = dsl.disableQualifiedArchiveBaseName,
    )

}
