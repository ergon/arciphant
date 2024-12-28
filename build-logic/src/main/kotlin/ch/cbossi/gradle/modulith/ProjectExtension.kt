package ch.cbossi.gradle.modulith

import org.gradle.api.Project

internal fun Project.childProject(name: String) = childProjects.getValue(name)