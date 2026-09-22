package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.model.Component

internal data class GlobalSettings(
    val disableFolderCreation: Boolean,
    val componentLayout: ComponentLayout,
    val projectComponentSettings: ProjectComponentSettings,
    val sourceSetComponentSettings: SourceSetComponentSettings,
)

internal sealed interface ComponentSettings

internal data class ProjectComponentSettings(
    val disableQualifiedArchiveBaseName: Boolean,
) : ComponentSettings

internal data class SourceSetComponentSettings(
    val withTestSourceSet: Boolean,
    val withTestFixturesSourceSet: Boolean,
    val testSourceSetName: (String) -> String,
    val testFixturesSourceSetName: (String) -> String,
) : ComponentSettings {
    companion object {
        internal val DEFAULT_SETTINGS = SourceSetComponentSettings(
            withTestSourceSet = true,
            withTestFixturesSourceSet = true,
            testSourceSetName = { "${it}Test" },
            testFixturesSourceSetName = { "${it}TestFixtures" },
        )
    }
}

internal fun Component.sourceSetNames(settings: SourceSetComponentSettings): List<String> {
    val name = reference.name
    return listOfNotNull(
        name,
        settings.testSourceSetName(name)
            .takeIf { withTestSourceSet ?: settings.withTestSourceSet },
        settings.testFixturesSourceSetName(name)
            .takeIf { withTestFixturesSourceSet ?: settings.withTestFixturesSourceSet },
    )
}
