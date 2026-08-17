package ch.ergon.arciphant.core

internal fun sourceSetComponentSettings(
    withTestSourceSet: Boolean = true,
    withTestFixturesSourceSet: Boolean = true,
    testSourceSetName: ((String) -> String) = { it.defaultTestSourceSetName() },
    testFixturesSourceSetName: ((String) -> String) = { it.defaultTestFixturesSourceSetName() },
) = SourceSetComponentSettings(
    withTestSourceSet = withTestSourceSet,
    withTestFixturesSourceSet = withTestFixturesSourceSet,
    testSourceSetName = testSourceSetName,
    testFixturesSourceSetName = testFixturesSourceSetName,
)
