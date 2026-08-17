package ch.ergon.arciphant.util

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

fun javaProject(name: String = "project", parent: Project? = null): Project {
    val builder = ProjectBuilder.builder().withName(name)
    if (parent != null) builder.withParent(parent)
    return builder.build().also { it.pluginManager.apply("java-library") }
}

fun Project.configuration(name: String) = configurations.getByName(name)