package ch.ergon.arciphant.core

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Before running this test with IntelliJ, the project should be built using Gradle.
 * See https://jdriven.com/blog/2021/01/gradlerunner-tests-intellij
 */
class ArciphantPluginTest {

    @TempDir
    private lateinit var projectFolder: File

    private val settingsFile by lazy { projectFolder.resolve("settings.gradle.kts") }
    private val buildFile by lazy { projectFolder.resolve("build.gradle.kts") }

    private val gradleRunner by lazy {
        GradleRunner.create()
            .withProjectDir(projectFolder)
            .withPluginClasspath()
            .forwardOutput()
    }

    @Test
    fun `test that library is created according to configuration`() {
        settingsFileWithArciphant(
            """
            val sampleTemplate = template()
                .createComponent("api")
                .createComponent("domain")
            
            library("test", template = sampleTemplate)
            """
        )
        val result = gradleRunner
            .withArguments("-q", "projects")
            .build()

        assertThat(result.output).contains("Project ':test:api'")
        assertThat(result.output).contains("Project ':test:domain'")
    }

    @Test
    fun `test that module is created according to configuration`() {
        settingsFileWithArciphant(
            """
            val sampleTemplate = template()
                .createComponent("api")
                .createComponent("domain")
            
            module("test", template = sampleTemplate)
            """
        )
        val result = gradleRunner
            .withArguments("-q", "projects")
            .build()

        assertThat(result.output).contains("Project ':test:api'")
        assertThat(result.output).contains("Project ':test:domain'")
    }

    @Test
    fun `test that package structure validation is scoped to the selected project`() {
        settingsFileWithArciphant(
            """
            packageStructureValidation {
                basePackageName("com.example")
            }

            val sampleTemplate = template()
                .createComponent("api")
                .createComponent("domain")

            module("test", template = sampleTemplate)
            """
        )
        projectFolder.resolve("test/api/src/main/kotlin/com/example/test/api/Valid.kt").write(
            """
            package com.example.test.api
            """
        )
        val invalidSourceFile = projectFolder.resolve("test/domain/src/main/kotlin/wrong/Invalid.kt")
        invalidSourceFile.write(
            """
            package wrong
            """
        )

        val validResult = gradleRunner
            .withArguments(":test:api:validatePackageStructure")
            .build()
        assertThat(validResult.task(":test:api:validatePackageStructure")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)

        val invalidResult = gradleRunner
            .withArguments("validatePackageStructure")
            .buildAndFail()
        assertThat(invalidResult.task(":test:domain:validatePackageStructure")?.outcome)
            .isEqualTo(TaskOutcome.FAILED)
        assertThat(invalidResult.output)
            .contains("Source file '${invalidSourceFile.path}' has invalid package name.")
    }

    @Test
    fun `test that components can be created as source sets`() {
        settingsFileWithArciphant(
            """
            sourceSetComponentLayout()

            module("test")
                .createComponent("domain")
                .createComponent("application", dependsOnApi = setOf("domain"))
            """
        )
        buildFileWithJvmPlugins()
        projectFolder.resolve("test/build.gradle.kts").write(
            """
            dependencies {
                add("applicationTestImplementation", "org.junit.jupiter:junit-jupiter:5.11.3")
                add("applicationTestRuntimeOnly", "org.junit.platform:junit-platform-launcher")
            }
            """
        )
        projectFolder.resolve("test/src/domain/java/example/domain/Domain.java").write(
            """
            package example.domain;

            public class Domain {
                public String value() { return "domain"; }
            }
            """
        )
        projectFolder.resolve("test/src/domainTestFixtures/java/example/domain/DomainFixtures.java").write(
            """
            package example.domain;

            public class DomainFixtures {
                public static Domain create() { return new Domain(); }
            }
            """
        )
        projectFolder.resolve("test/src/application/java/example/application/Application.java").write(
            """
            package example.application;

            import example.domain.Domain;

            public class Application {
                public Domain domain() { return new Domain(); }
            }
            """
        )
        projectFolder.resolve("test/src/applicationTestFixtures/java/example/application/ApplicationFixtures.java").write(
            """
            package example.application;

            import example.domain.DomainFixtures;

            public class ApplicationFixtures {
                public static Application create() {
                    DomainFixtures.create();
                    return new Application();
                }
            }
            """
        )
        projectFolder.resolve("test/src/applicationTest/java/example/application/ApplicationTest.java").write(
            """
            package example.application;

            import org.junit.jupiter.api.Test;

            import static org.junit.jupiter.api.Assertions.assertNotNull;

            class ApplicationTest {
                @Test
                void createsApplication() {
                    assertNotNull(ApplicationFixtures.create());
                }
            }
            """
        )

        val projectsResult = gradleRunner.withArguments("-q", "projects").build()
        val buildResult = gradleRunner.withArguments(":test:build").build()

        assertThat(projectsResult.output).contains("Project ':test'")
        assertThat(projectsResult.output).doesNotContain("Project ':test:domain'")
        assertThat(buildResult.task(":test:domainClasses")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(buildResult.task(":test:applicationClasses")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(buildResult.task(":test:applicationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `test that library source sets are consumed by modules`() {
        settingsFileWithArciphant(
            """
            sourceSetComponentLayout()

            val template = template().createComponent("api")
            library("shared", template = template)
            module("module", template = template)
            """
        )
        buildFileWithJvmPlugins()
        projectFolder.resolve("module/build.gradle.kts").write(
            """
            dependencies {
                add("apiTestImplementation", "org.junit.jupiter:junit-jupiter:5.11.3")
                add("apiTestRuntimeOnly", "org.junit.platform:junit-platform-launcher")
            }
            """
        )
        projectFolder.resolve("shared/src/api/java/example/shared/SharedApi.java").write(
            """
            package example.shared;

            public class SharedApi {}
            """
        )
        projectFolder.resolve("shared/src/apiTestFixtures/java/example/shared/SharedFixtures.java").write(
            """
            package example.shared;

            public class SharedFixtures {
                public static SharedApi create() { return new SharedApi(); }
            }
            """
        )
        projectFolder.resolve("module/src/api/java/example/module/ModuleApi.java").write(
            """
            package example.module;

            import example.shared.SharedApi;

            public class ModuleApi extends SharedApi {}
            """
        )
        projectFolder.resolve("module/src/apiTestFixtures/java/example/module/ModuleFixtures.java").write(
            """
            package example.module;

            import example.shared.SharedFixtures;

            public class ModuleFixtures {
                public static ModuleApi create() {
                    SharedFixtures.create();
                    return new ModuleApi();
                }
            }
            """
        )
        projectFolder.resolve("module/src/apiTest/java/example/module/ModuleApiTest.java").write(
            """
            package example.module;

            import org.junit.jupiter.api.Test;

            import static org.junit.jupiter.api.Assertions.assertNotNull;

            class ModuleApiTest {
                @Test
                void createsModuleApi() {
                    assertNotNull(ModuleFixtures.create());
                }
            }
            """
        )

        val result = gradleRunner.withArguments(":module:test").build()

        assertThat(result.task(":shared:compileApiJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":shared:compileApiTestFixturesJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module:apiTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `test that source set names and flags can be overridden`() {
        settingsFileWithArciphant(
            """
            sourceSetComponentLayout()
            withTestSourceSet(false)
            withTestFixturesSourceSet(false)
            testSourceSetName { "${'$'}{it}Spec" }
            testFixturesSourceSetName { "${'$'}{it}Fixtures" }

            module("test").createComponent(
                name = "domain",
                withTestSourceSet = true,
                withTestFixturesSourceSet = true,
            )
            """
        )
        buildFileWithJvmPlugins()
        projectFolder.resolve("test/build.gradle.kts").write(
            """
            dependencies {
                add("domainSpecImplementation", "org.junit.jupiter:junit-jupiter:5.11.3")
                add("domainSpecRuntimeOnly", "org.junit.platform:junit-platform-launcher")
            }
            """
        )
        projectFolder.resolve("test/src/domain/java/example/Domain.java").write(
            """
            package example;

            public class Domain {}
            """
        )
        projectFolder.resolve("test/src/domainFixtures/java/example/DomainFixtures.java").write(
            """
            package example;

            public class DomainFixtures {
                public static Domain create() { return new Domain(); }
            }
            """
        )
        projectFolder.resolve("test/src/domainSpec/java/example/DomainSpec.java").write(
            """
            package example;

            import org.junit.jupiter.api.Test;

            import static org.junit.jupiter.api.Assertions.assertNotNull;

            class DomainSpec {
                @Test
                void createsDomain() {
                    assertNotNull(DomainFixtures.create());
                }
            }
            """
        )

        val result = gradleRunner.withArguments(":test:test").build()

        assertThat(result.task(":test:domainSpec")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":test:compileDomainFixturesJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `test that components of other modules can be used through project DSL`() {
        settingsFileWithArciphant(
            """
            sourceSetComponentLayout()

            module("producer").createComponent("api")
            module("consumer").createComponent("application")
            """
        )
        buildFileWithJvmPlugins()
        projectFolder.resolve("consumer/build.gradle.kts").write(
            """
            arciphantModule {
                component("application").implementation(module = "producer", component = "api")
            }
            """
        )
        projectFolder.resolve("producer/src/api/java/example/producer/Api.java").write(
            """
            package example.producer;

            public class Api {}
            """
        )
        projectFolder.resolve("producer/src/apiTestFixtures/java/example/producer/ApiFixtures.java").write(
            """
            package example.producer;

            public class ApiFixtures {
                public static Api create() { return new Api(); }
            }
            """
        )
        projectFolder.resolve("consumer/src/application/java/example/consumer/Application.java").write(
            """
            package example.consumer;

            import example.producer.Api;

            public class Application extends Api {}
            """
        )
        projectFolder.resolve("consumer/src/applicationTestFixtures/java/example/consumer/ApplicationFixtures.java").write(
            """
            package example.consumer;

            import example.producer.ApiFixtures;

            public class ApplicationFixtures {
                public static Application create() {
                    ApiFixtures.create();
                    return new Application();
                }
            }
            """
        )

        val result = gradleRunner.withArguments(":consumer:compileApplicationTestFixturesJava").build()

        assertThat(result.task(":producer:compileApiJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":producer:compileApiTestFixturesJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":consumer:compileApplicationTestFixturesJava")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `test that bundles consume source set modules`() {
        settingsFileWithArciphant(
            """
            sourceSetComponentLayout()

            module("module").createComponent("api")
            bundle("application")
            """
        )
        buildFileWithJvmPlugins()
        projectFolder.resolve("module/src/api/java/example/module/Api.java").write(
            """
            package example.module;

            public class Api {}
            """
        )
        projectFolder.resolve("application/src/main/java/example/application/Application.java").write(
            """
            package example.application;

            import example.module.Api;

            public class Application extends Api {}
            """
        )

        val result = gradleRunner.withArguments(":application:compileJava").build()

        assertThat(result.task(":module:compileApiJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module:jar")).isNull()
        assertThat(result.task(":application:compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `test that Kotlin component source sets are associated`() {
        settingsFileWithArciphant(
            """
            sourceSetComponentLayout()

            module("module")
                .createComponent("domain")
                .createComponent("application", dependsOn = setOf("domain"))
            """
        )
        buildFile.write(
            """
            plugins {
                kotlin("jvm") version "2.2.0" apply false
            }

            allprojects {
                pluginManager.apply("org.jetbrains.kotlin.jvm")
                pluginManager.apply("java-library")
                repositories { mavenCentral() }
            }
            """
        )
        projectFolder.resolve("module/src/domain/kotlin/example/domain/Domain.kt").write(
            """
            package example.domain

            internal class Domain
            """
        )
        projectFolder.resolve("module/src/domainTestFixtures/kotlin/example/domain/DomainFixtures.kt").write(
            """
            package example.domain

            fun createDomain(): Any = Domain()
            """
        )
        projectFolder.resolve("module/src/application/kotlin/example/application/Application.kt").write(
            """
            package example.application

            class Application
            """
        )
        projectFolder.resolve("module/src/applicationTestFixtures/kotlin/example/application/ApplicationFixtures.kt").write(
            """
            package example.application

            import example.domain.createDomain

            fun createApplication(): Application {
                createDomain()
                return Application()
            }
            """
        )

        val result = gradleRunner.withArguments(":module:compileApplicationTestFixturesKotlin").build()

        assertThat(result.task(":module:compileDomainKotlin")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module:compileDomainTestFixturesKotlin")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module:compileApplicationTestFixturesKotlin")?.outcome)
            .isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `test that source set folders are created according to configuration`() {
        settingsFileWithArciphant(
            """
            sourceSetComponentLayout()
            withTestFixturesSourceSet(false)

            module("test")
                .createComponent("domain", withTestFixturesSourceSet = true)
                .createComponent("application", withTestSourceSet = false)
            """
        )
        buildFileWithJvmPlugins()

        gradleRunner.withArguments("-q", "projects").build()

        assertThat(projectFolder.resolve("test/src/domain")).isDirectory()
        assertThat(projectFolder.resolve("test/src/domainTest")).isDirectory()
        assertThat(projectFolder.resolve("test/src/domainTestFixtures")).isDirectory()
        assertThat(projectFolder.resolve("test/src/application")).isDirectory()
        assertThat(projectFolder.resolve("test/src/applicationTest")).doesNotExist()
        assertThat(projectFolder.resolve("test/src/applicationTestFixtures")).doesNotExist()
    }

    private fun settingsFileWithArciphant(arciphantConfiguration: String) = settingsFile.write(
        """
                plugins {
                    id("ch.ergon.arciphant")
                }
                
                arciphant {
                    $arciphantConfiguration
                }
                """
    )

    private fun buildFileWithJvmPlugins(additionalConfiguration: String = "") = buildFile.write(
        """
        import org.gradle.api.tasks.testing.Test

        allprojects {
            pluginManager.apply("java-library")
            repositories { mavenCentral() }
            tasks.withType<Test>().configureEach { useJUnitPlatform() }
        }

        $additionalConfiguration
        """
    )

    private fun File.write(content: String) {
        parentFile.mkdirs()
        writeText(content.trimIndent())
    }
}
