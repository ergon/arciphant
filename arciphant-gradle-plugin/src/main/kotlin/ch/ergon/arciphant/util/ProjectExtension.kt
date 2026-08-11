package ch.ergon.arciphant.util

import org.gradle.api.Project

internal fun Project.projectHierarchy() = generateSequence(this) { it.parent }.takeWhile { it.parent != null }.toList().asReversed()
