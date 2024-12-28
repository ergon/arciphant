package ch.cbossi.gradle.modulith

fun String?.emptyToNull() = this?.let { if (isEmpty()) null else this }