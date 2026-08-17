package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.core.*
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

open class ArciphantProjectDsl internal constructor(private val project: Project, private val componentSettings: SourceSetComponentSettings) {

    fun createComponent(
        name: String,
        withTestSourceSet: Boolean? = null,
        withTestFixturesSourceSet: Boolean? = null,
        consumable: Boolean = false,
        sourceSetDependencies: (SourceSetDependencyScope.(SourceSet) -> Unit)? = null,
    ): SourceSet = project.createComponent(
        name = name,
        settings = componentSettings,
        withTestSourceSet = withTestSourceSet,
        withTestFixturesSourceSet = withTestFixturesSourceSet,
        consumable = consumable,
        sourceSetDependenciesBlock = sourceSetDependencies,
    ).production

    fun sourceSetDependencies(block: SourceSetDependencyScope.() -> Unit) {
        project.sourceSetDependencies(componentSettings, block)
    }
}
