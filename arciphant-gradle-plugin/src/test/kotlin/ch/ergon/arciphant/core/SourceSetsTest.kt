package ch.ergon.arciphant.core

import ch.ergon.arciphant.util.configuration
import ch.ergon.arciphant.util.hasFileDependencyOn
import ch.ergon.arciphant.util.javaProject
import ch.ergon.arciphant.util.projectDependencyConfigurations
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.tasks.SourceSet
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.gradle.api.tasks.testing.Test as GradleTest

class SourceSetsTest {

    private val settings = sourceSetComponentSettings()

    @Test
    fun `it should create component source sets and test task`() {
        val project = javaProject()

        val component = project.createComponent(name = "domain", settings = settings)

        assertThat(component.production.name).isEqualTo("domain")
        assertThat(component.testFixtures?.name).isEqualTo("domainTestFixtures")
        assertThat(component.test?.name).isEqualTo("domainTest")
        assertThat(project.tasks.getByName("domainTest")).isInstanceOf(GradleTest::class.java)
        assertThat(project.tasks.getByName("test").taskDependencies.getDependencies(project.tasks.getByName("test")))
            .contains(project.tasks.getByName("domainTest"))
    }

    @Test
    fun `it should support optional test source sets`() {
        val project = javaProject()

        val withoutTestFixtures = project.createComponent(
            name = "domain",
            settings = settings,
            withTestFixturesSourceSet = false,
        )
        val withoutTest = project.createComponent(
            name = "api",
            settings = settings,
            withTestSourceSet = false,
        )

        assertThat(withoutTestFixtures.testFixtures).isNull()
        assertThat(withoutTestFixtures.test).isNotNull()
        assertThat(project.configuration("domainTestImplementation").hasFileDependencyOn(withoutTestFixtures.production))
            .isTrue()
        assertThat(withoutTest.testFixtures).isNotNull()
        assertThat(withoutTest.test).isNull()
        assertThat(project.tasks.findByName("apiTest")).isNull()
    }

    @Test
    fun `it should fall back to the global test source set settings`() {
        val project = javaProject()

        val component = project.createComponent(
            name = "domain",
            settings = sourceSetComponentSettings(withTestSourceSet = false, withTestFixturesSourceSet = false),
        )

        assertThat(component.test).isNull()
        assertThat(component.testFixtures).isNull()
    }

    @Test
    fun `it should create consumable API and runtime configurations`() {
        val project = javaProject()

        project.createComponent(name = "domain", settings = settings)
        project.createComponent(name = "api", settings = settings, consumable = true)

        assertThat(project.configurations.findByName("domainApiElements")).isNull()
        assertThat(project.configuration("apiApiElements").isCanBeConsumed).isTrue()
        assertThat(project.configuration("apiRuntimeElements").isCanBeConsumed).isTrue()
        assertThat(project.configuration("apiTestFixturesApiElements").isCanBeConsumed).isTrue()
        assertThat(project.configuration("apiTestFixturesRuntimeElements").isCanBeConsumed).isTrue()
    }

    @Test
    fun `it should include component output and API in main`() {
        val project = javaProject()

        project.createComponent(name = "domain", settings = settings)

        assertThat(project.configuration("api").extendsFrom).contains(project.configuration("domainApi"))
        assertThat(project.configuration("implementation").extendsFrom)
            .contains(project.configuration("domainImplementation"))
    }

    @Test
    fun `it should mark tests and test fixtures as IDEA test sources`() {
        val project = javaProject().also { it.pluginManager.apply("idea") }

        val component = project.createComponent(name = "domain", settings = settings)

        val testSources = project.extensions.getByType(IdeaModel::class.java).module.testSources.files
        assertThat(testSources).containsAll(component.testFixtures!!.allSource.srcDirs)
        assertThat(testSources).containsAll(component.test!!.allSource.srcDirs)
    }

    @Test
    fun `it should mirror local component dependencies to test fixtures only`() {
        val project = javaProject()
        val target = project.createComponent(name = "domain", settings = settings)
        val source = project.createComponent(name = "application", settings = settings) { sourceSet ->
            implementation(sourceSet, target.production)
        }

        assertThat(project.configuration("applicationImplementation").hasFileDependencyOn(target.production))
            .isTrue()
        assertThat(
            project.configuration("applicationTestFixturesImplementation").hasFileDependencyOn(target.testFixtures!!)
        ).isTrue()
        assertThat(project.configuration("applicationTestImplementation").hasFileDependencyOn(target.test!!))
            .isFalse()
        assertThat(source.test).isNotNull()
    }

    @Test
    fun `it should mirror local component dependencies with custom source set names`() {
        val customSettings = sourceSetComponentSettings(
            testSourceSetName = { "${it}Spec" },
            testFixturesSourceSetName = { "${it}Fixtures" },
        )
        val project = javaProject()
        val target = project.createComponent(name = "domain", settings = customSettings)
        val source = project.createComponent(name = "application", settings = customSettings) { sourceSet ->
            implementation(sourceSet, target.production)
        }

        assertThat(target.testFixtures?.name).isEqualTo("domainFixtures")
        assertThat(source.test?.name).isEqualTo("applicationSpec")
        assertThat(
            project.configuration("applicationFixturesImplementation").hasFileDependencyOn(target.testFixtures!!)
        ).isTrue()
    }

    @Test
    fun `it should add project dependencies to consumable source set configurations`() {
        val customSettings = sourceSetComponentSettings(testFixturesSourceSetName = { "${it}Fixtures" })
        val root = javaProject("root")
        javaProject("library", root).createComponent(
            name = "domain",
            settings = customSettings,
            consumable = true,
        )
        val module = javaProject("module", root)
        val source = module.createComponent(name = "application", settings = customSettings)

        module.sourceSetDependencies(customSettings) {
            api(
                sourceSet = source.production,
                projectPath = ":library",
                componentName = "domain",
            )
        }

        assertThat(module.configuration("applicationApi").projectDependencyConfigurations())
            .containsExactly("domainApiElements")
        assertThat(module.configuration("applicationRuntimeOnly").projectDependencyConfigurations())
            .containsExactly("domainRuntimeElements")
        assertThat(module.configuration("applicationFixturesApi").projectDependencyConfigurations())
            .containsExactly("domainFixturesApiElements")
        assertThat(module.configuration("applicationFixturesRuntimeOnly").projectDependencyConfigurations())
            .containsExactly("domainFixturesRuntimeElements")
    }

    @Test
    fun `it should skip the test fixtures dependency if the target has none`() {
        val root = javaProject("root")
        javaProject("library", root).createComponent(
            name = "domain",
            settings = settings,
            withTestFixturesSourceSet = false,
            consumable = true,
        )
        val module = javaProject("module", root)
        val source = module.createComponent(name = "application", settings = settings)

        module.sourceSetDependencies(settings) {
            api(
                sourceSet = source.production,
                projectPath = ":library",
                componentName = "domain",
                withTestFixturesSourceSet = false,
            )
        }

        assertThat(module.configuration("applicationApi").projectDependencyConfigurations())
            .containsExactly("domainApiElements")
        assertThat(module.configuration("applicationTestFixturesApi").projectDependencyConfigurations()).isEmpty()
    }

    @Test
    fun `it should require a JVM plugin`() {
        val project = ProjectBuilder.builder().build()

        val exception = assertThrows<IllegalArgumentException> {
            project.createComponent(name = "domain", settings = settings)
        }

        assertThat(exception.message).isEqualTo("Arciphant error: cannot access source sets in project ':' because no compatible JVM plugin has been applied.")
    }

}
