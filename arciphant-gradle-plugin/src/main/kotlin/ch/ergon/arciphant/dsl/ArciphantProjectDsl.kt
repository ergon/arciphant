package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.core.*
import ch.ergon.arciphant.core.sourceset.SourceSetDependencyScope
import ch.ergon.arciphant.core.sourceset.SourceSetFactory
import ch.ergon.arciphant.core.sourceset.sourceSetDependencies
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

open class ArciphantProjectDsl internal constructor(private val project: Project, private val componentSettings: SourceSetComponentSettings) {

    fun createComponent(
        name: String,
        withTestSourceSet: Boolean? = null,
        withTestFixturesSourceSet: Boolean? = null,
        sourceSetDependencies: (SourceSetDependencyScope.(SourceSet) -> Unit)? = null,
    ): SourceSet = SourceSetFactory(project).createComponent(
        name = name,
        settings = componentSettings,
        withTestSourceSet = withTestSourceSet,
        withTestFixturesSourceSet = withTestFixturesSourceSet,
        sourceSetDependencies = sourceSetDependencies,
    ).production

    fun sourceSetDependencies(block: SourceSetDependencyScope.() -> Unit) {
        project.sourceSetDependencies(componentSettings, block)
    }
}
