package ch.ergon.arciphant.core.model

internal fun component(
    reference: ComponentReference,
    plugin: Plugin? = null,
    dependsOn: Set<Dependency> = emptySet(),
    withTestSourceSet: Boolean? = null,
    withTestFixturesSourceSet: Boolean? = null,
) = Component(
    reference = reference,
    plugin = plugin,
    dependsOn = dependsOn,
    withTestSourceSet = withTestSourceSet,
    withTestFixturesSourceSet = withTestFixturesSourceSet,
)
