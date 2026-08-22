package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.sourceSetComponentSettings
import ch.ergon.arciphant.core.sourceset.sourceSets
import ch.ergon.arciphant.util.configuration
import ch.ergon.arciphant.util.hasFileDependencyOn
import ch.ergon.arciphant.util.javaProject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ArciphantProjectDslTest {

    @Test
    fun `it should expose component utilities through project DSL`() {
        val project = javaProject()
        val settings = sourceSetComponentSettings(
            testSourceSetName = { "${it}Spec" },
            testFixturesSourceSetName = { "${it}Fixtures" },
        )
        val dsl = ArciphantProjectDsl(project, settings)

        val domain = dsl.createComponent(name = "domain")
        dsl.createComponent(
            name = "application",
            sourceSetDependencies = { application -> addLocalDependency(API, application, domain) },
        )

        assertThat(project.sourceSets().names)
            .contains("application", "applicationSpec", "applicationFixtures")
        assertThat(project.configuration("applicationApi").hasFileDependencyOn(domain)).isTrue()
        assertThat(
            project.configuration("applicationFixturesApi")
                .hasFileDependencyOn(project.sourceSets().getByName("domainFixtures"))
        ).isTrue()
    }

}
