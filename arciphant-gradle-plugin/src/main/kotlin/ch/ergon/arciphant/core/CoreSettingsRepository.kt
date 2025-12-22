package ch.ergon.arciphant.core

import ch.ergon.arciphant.dsl.ArciphantDsl

internal class CoreSettingsRepository(private val dsl: ArciphantDsl) {

    fun load() = CoreSettings(
        disableQualifiedArchiveBaseName = dsl.disableQualifiedArchiveBaseName
    )

}
