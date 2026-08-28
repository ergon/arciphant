package ch.ergon.arciphant.core

internal data class CoreSettings(
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
