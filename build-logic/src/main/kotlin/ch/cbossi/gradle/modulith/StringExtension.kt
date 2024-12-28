package ch.cbossi.gradle.modulith

internal fun String?.emptyToNull() = this?.let { if (isEmpty()) null else this }