package ch.ergon.arciphant.core

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

fun Project.createComponent(
    name: String,
    withTesting: Boolean = true,
    sourceSetDependenciesBlock: (SourceSetDependencyScope.(SourceSet) -> Unit)? = null,
): SourceSet {
    val sourceSet = sourceSets.create(name)

    createConsumableConfiguration(sourceSet)

    if (withTesting) createTestSourceSets(sourceSet)

    sourceSetDependencies {
        if(sourceSetDependenciesBlock != null) {
            sourceSetDependenciesBlock(sourceSet)
        }
    }

    makeConfigurationsInvisibleToOtherProjects(sourceSet)
    includeInMainSourceSet(sourceSet)

    return sourceSet
}

/**
 * Creates the `<name>TestFixtures` and `<name>Test` source sets belonging to [sourceSet], associates them
 * with each other and registers a `Test` task for the test source set.
 */
fun Project.createTestSourceSets(sourceSet: SourceSet) {
    val testFixturesSourceSet = sourceSets().create(sourceSet.getCorrespondingTestFixturesSourceSetName())
    val testSourceSet = sourceSets().create(sourceSet.getCorrespondingTestSourceSetName())

    associate(testFixturesSourceSet, sourceSet)
    associate(testSourceSet, testFixturesSourceSet)

    createConsumableConfiguration(testFixturesSourceSet)

    registerTestTask(testSourceSet)

    if (pluginManager.hasPlugin("idea")) {
        val ideaModule = extensions.getByType(IdeaModel::class.java).module
        ideaModule.testSources.from(testFixturesSourceSet.allSource.srcDirs)
        ideaModule.testSources.from(testSourceSet.allSource.srcDirs)
    }
}

internal fun Project.sourceSets() = project.extensions.getByType(SourceSetContainer::class.java)

private fun Project.associate(sourceSet: SourceSet, other: SourceSet) =
    compilationOf(sourceSet).associateWith(compilationOf(other))

private fun Project.compilationOf(sourceSet: SourceSet) =
    extensions.getByType(KotlinJvmProjectExtension::class.java).target.compilations.getByName(sourceSet.name)

/** Makes the classes of [sourceSet] as well as its dependencies available to consuming projects. */
fun Project.createConsumableConfiguration(sourceSet: SourceSet) {
    val consumableConfiguration = configurations.create(sourceSet.name) {
        description =
            "configuration to be used by other projects depending on this project's '${sourceSet.name}' source set"
    }
    consumableConfiguration.extendsFrom(configurations.getByName(sourceSet.implementationConfigurationName))
    dependencies.add(sourceSet.name, sourceSet.output)
}

// registers a Test task for the custom test source set and hooks it into the default `test` task
private fun Project.registerTestTask(testSourceSet: SourceSet) {
    val testTask = tasks.register(testSourceSet.name, Test::class.java) {
        description = "Runs the tests of the '${testSourceSet.name}' source set."
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        val compilation = compilationOf(testSourceSet)
        testClassesDirs = compilation.output.classesDirs
        classpath = compilation.output.allOutputs + compilation.runtimeDependencyFiles
    }
    tasks.named("test").configure { dependsOn(testTask) }
}

private fun Project.includeInMainSourceSet(sourceSet: SourceSet) {
    val mainSourceSet = sourceSets().getByName(MAIN_SOURCE_SET_NAME)

    mainSourceSet.compileClasspath += sourceSet.output
    mainSourceSet.runtimeClasspath += sourceSet.output
    mainSourceSet.resources.srcDirs += sourceSet.resources.srcDirs

    val implementationConfiguration = configurations.getByName("implementation")
    val runtimeOnlyConfiguration = configurations.getByName("runtimeOnly")

    implementationConfiguration.extendsFrom(getConfiguration(sourceSet.implementationConfigurationName))
    runtimeOnlyConfiguration.extendsFrom(getConfiguration(sourceSet.runtimeOnlyConfigurationName))
}

private fun Project.makeConfigurationsInvisibleToOtherProjects(sourceSet: SourceSet) {
    getConfiguration(sourceSet.implementationConfigurationName).isVisible = false
    getConfiguration(sourceSet.runtimeOnlyConfigurationName).isVisible = false
}

fun Project.getConfiguration(configurationName: String) = project.configurations.getByName(configurationName)

internal fun SourceSet.getCorrespondingTestSourceSetName() = name.getCorrespondingTestSourceSetName()
internal fun SourceSet.getCorrespondingTestFixturesSourceSetName() = name.getCorrespondingTestFixturesSourceSetName()

internal fun String.getCorrespondingTestSourceSetName() = "${this}Test"
internal fun String.getCorrespondingTestFixturesSourceSetName() = "${this}TestFixtures"
