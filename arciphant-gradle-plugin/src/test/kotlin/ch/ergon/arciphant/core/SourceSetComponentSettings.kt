package ch.ergon.arciphant.core

internal fun sourceSetComponentSettings(
    withTestSourceSet: Boolean = true,
    withTestFixturesSourceSet: Boolean = true,
    testSourceSetName: ((String) -> String) = { "${it}Test" },
    testFixturesSourceSetName: ((String) -> String) = { "${it}TestFixtures" },
) = SourceSetComponentSettings(
    withTestSourceSet = withTestSourceSet,
    withTestFixturesSourceSet = withTestFixturesSourceSet,
    testSourceSetName = testSourceSetName,
    testFixturesSourceSetName = testFixturesSourceSetName,
)
