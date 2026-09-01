package ch.ergon.arciphant.core.sourceset

import ch.ergon.arciphant.core.GradlePluginIds.IDEA
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.sourceSetComponentSettings
import ch.ergon.arciphant.util.configuration
import ch.ergon.arciphant.util.hasFileDependencyOn
import ch.ergon.arciphant.util.javaProject
import ch.ergon.arciphant.util.projectDependencyConfigurations
import org.assertj.core.api.Assertions.assertThat
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.gradle.api.tasks.testing.Test as GradleTest

class SourceSetFactoryTest {

    private val settings = sourceSetComponentSettings()

    @Test
    fun `it should create component source sets and test task`() {
        val project = javaProject()

        val factory = SourceSetFactory(project)
        val component = factory.createComponent(name = "domain", settings = settings)

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

        val factory = SourceSetFactory(project)
        val withoutTestFixtures = factory.createComponent(
            name = "domain",
            settings = settings,
            withTestFixturesSourceSet = false,
        )
        val withoutTest = factory.createComponent(
            name = "api",
            settings = settings,
            withTestSourceSet = false,
        )

        assertThat(withoutTestFixtures.testFixtures).isNull()
        assertThat(withoutTestFixtures.test).isNotNull()
        assertThat(
            project.configuration("domainTestImplementation").hasFileDependencyOn(withoutTestFixtures.production)
        ).isTrue()
        assertThat(withoutTest.testFixtures).isNotNull()
        assertThat(withoutTest.test).isNull()
        assertThat(project.tasks.findByName("apiTest")).isNull()
    }

    @Test
    fun `it should fall back to the global test source set settings`() {
        val project = javaProject()

        val factory = SourceSetFactory(project)
        val component = factory.createComponent(
            name = "domain",
            settings = sourceSetComponentSettings(withTestSourceSet = false, withTestFixturesSourceSet = false),
        )

        assertThat(component.test).isNull()
        assertThat(component.testFixtures).isNull()
    }

    @Test
    fun `it should create consumable API and runtime configurations`() {
        val project = javaProject()

        val factory = SourceSetFactory(project)
        factory.createComponent(name = "domain", settings = settings)
        factory.createComponent(name = "api", settings = settings)

        assertThat(project.configuration("domainApiElements").isCanBeConsumed).isTrue()
        assertThat(project.configuration("domainRuntimeElements").isCanBeConsumed).isTrue()
        assertThat(project.configuration("apiApiElements").isCanBeConsumed).isTrue()
        assertThat(project.configuration("apiRuntimeElements").isCanBeConsumed).isTrue()
        assertThat(project.configuration("apiTestFixturesApiElements").isCanBeConsumed).isTrue()
        assertThat(project.configuration("apiTestFixturesRuntimeElements").isCanBeConsumed).isTrue()
    }

    @Test
    fun `it should extend the shared dependency configurations`() {
        val project = javaProject()

        SourceSetFactory(project).createComponent(name = "domain", settings = settings)

        fun assertExtendsFrom(configuration: String, sharedConfiguration: String) =
            assertThat(project.configuration(configuration).extendsFrom)
                .contains(project.configuration(sharedConfiguration))

        assertExtendsFrom("domainApi", "api")
        assertExtendsFrom("domainImplementation", "implementation")
        assertExtendsFrom("domainCompileOnly", "compileOnly")
        assertExtendsFrom("domainRuntimeOnly", "runtimeOnly")
        assertExtendsFrom("domainTestImplementation", "testImplementation")
        assertExtendsFrom("domainTestCompileOnly", "testCompileOnly")
        assertExtendsFrom("domainTestRuntimeOnly", "testRuntimeOnly")
        assertExtendsFrom("domainTestFixturesApi", "testFixturesApi")
        assertExtendsFrom("domainTestFixturesImplementation", "testFixturesImplementation")
        assertExtendsFrom("domainTestFixturesCompileOnly", "testFixturesCompileOnly")
        assertExtendsFrom("domainTestFixturesRuntimeOnly", "testFixturesRuntimeOnly")
    }

    @Test
    fun `it should create the shared test fixtures dependency configurations`() {
        val project = ProjectBuilder.builder().build().also { it.pluginManager.apply("java") }

        SourceSetFactory(project).createComponent(name = "domain", settings = settings)

        assertThat(project.configuration("testFixturesImplementation").isCanBeConsumed).isFalse()
        assertThat(project.configuration("testFixturesImplementation").isCanBeResolved).isFalse()
        assertThat(project.configurations.names).contains("testFixturesCompileOnly", "testFixturesRuntimeOnly")
    }

    @Test
    fun `it should only provide the shared api configurations with the java-library plugin`() {
        // plain java plugin: neither 'api' nor 'testFixturesApi' exist
        val project = ProjectBuilder.builder().build().also { it.pluginManager.apply("java") }

        SourceSetFactory(project).createComponent(name = "domain", settings = settings)

        assertThat(project.configurations.names).doesNotContain("api", "testFixturesApi")

        project.pluginManager.apply("java-library")

        assertThat(project.configuration("domainApi").extendsFrom).contains(project.configuration("api"))
        assertThat(project.configuration("domainTestFixturesApi").extendsFrom)
            .contains(project.configuration("testFixturesApi"))
        assertThat(project.configuration("testFixturesApi").isCanBeConsumed).isFalse()
    }

    @Test
    fun `it should attach component source sets to the lifecycle tasks`() {
        val project = javaProject()

        val factory = SourceSetFactory(project)
        factory.createComponent(name = "domain", settings = settings)

        val classes = project.tasks.getByName("classes")
        val testClasses = project.tasks.getByName("testClasses")
        assertThat(classes.taskDependencies.getDependencies(classes))
            .contains(project.tasks.getByName("domainClasses"))
        assertThat(testClasses.taskDependencies.getDependencies(testClasses))
            .contains(
                project.tasks.getByName("domainTestClasses"),
                project.tasks.getByName("domainTestFixturesClasses")
            )
    }

    @Test
    fun `it should attach component source sets to the default java compile tasks`() {
        val project = javaProject()

        val factory = SourceSetFactory(project)
        factory.createComponent(name = "domain", settings = settings)

        val compileJava = project.tasks.getByName("compileJava")
        val compileTestJava = project.tasks.getByName("compileTestJava")
        assertThat(compileJava.taskDependencies.getDependencies(compileJava))
            .contains(project.tasks.getByName("compileDomainJava"))
        assertThat(compileTestJava.taskDependencies.getDependencies(compileTestJava))
            .contains(
                project.tasks.getByName("compileDomainTestJava"),
                project.tasks.getByName("compileDomainTestFixturesJava")
            )
    }

    @Test
    fun `it should mark tests and test fixtures as IDEA test sources`() {
        val project = javaProject().also { it.pluginManager.apply(IDEA) }

        val factory = SourceSetFactory(project)
        val component = factory.createComponent(name = "domain", settings = settings)

        val testSources = project.extensions.getByType(IdeaModel::class.java).module.testSources.files
        assertThat(testSources).containsAll(component.testFixtures!!.allSource.srcDirs)
        assertThat(testSources).containsAll(component.test!!.allSource.srcDirs)
    }

    @Test
    fun `it should mirror local component dependencies to test fixtures only`() {
        val project = javaProject()
        val factory = SourceSetFactory(project)
        val target = factory.createComponent(name = "domain", settings = settings)
        val source = factory.createComponent(name = "application", settings = settings)

        SourceSetDependencyFactory(project, settings)
            .addIntraModuleDependency(IMPLEMENTATION, source.production, target.production)

        assertThat(project.configuration("applicationImplementation").hasFileDependencyOn(target.production)).isTrue()
        assertThat(
            project.configuration("applicationTestFixturesImplementation").hasFileDependencyOn(target.testFixtures!!)
        ).isTrue()
        assertThat(project.configuration("applicationTestImplementation").hasFileDependencyOn(target.test!!)).isFalse()
        assertThat(source.test).isNotNull()
    }

    @Test
    fun `it should mirror local component dependencies with custom source set names`() {
        val customSettings = sourceSetComponentSettings(
            testSourceSetName = { "${it}Spec" },
            testFixturesSourceSetName = { "${it}Fixtures" },
        )
        val project = javaProject()
        val factory = SourceSetFactory(project)
        val target = factory.createComponent(name = "domain", settings = customSettings)
        val source = factory.createComponent(name = "application", settings = customSettings)

        SourceSetDependencyFactory(project, customSettings)
            .addIntraModuleDependency(IMPLEMENTATION, source.production, target.production)

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
        SourceSetFactory(javaProject("library", root)).createComponent(
            name = "domain",
            settings = customSettings,
        )
        val module = javaProject("module", root)
        val source = SourceSetFactory(module).createComponent(name = "application", settings = customSettings)

        SourceSetDependencyFactory(module, customSettings).addInterModuleDependency(
            type = API,
            sourceSet = source.production,
            projectPath = ":library",
            componentName = "domain",
        )

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
        SourceSetFactory(javaProject("library", root)).createComponent(
            name = "domain",
            settings = settings,
            withTestFixturesSourceSet = false,
        )
        val module = javaProject("module", root)
        val source = SourceSetFactory(module).createComponent(name = "application", settings = settings)

        SourceSetDependencyFactory(module, settings).addInterModuleDependency(
            type = API,
            sourceSet = source.production,
            projectPath = ":library",
            componentName = "domain",
            withTestFixturesSourceSet = false,
        )

        assertThat(module.configuration("applicationApi").projectDependencyConfigurations())
            .containsExactly("domainApiElements")
        assertThat(module.configuration("applicationTestFixturesApi").projectDependencyConfigurations()).isEmpty()
    }

    @Test
    fun `it should require a JVM plugin`() {
        val project = ProjectBuilder.builder().build()

        val exception = assertThrows<IllegalArgumentException> {
            SourceSetFactory(project).createComponent(name = "domain", settings = settings)
        }

        assertThat(exception.message).isEqualTo("Arciphant error: cannot access source sets in project ':' because no compatible JVM plugin has been applied.")
    }

}
