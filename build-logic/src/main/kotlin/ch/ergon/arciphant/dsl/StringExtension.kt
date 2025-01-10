package ch.ergon.arciphant.dsl

internal fun String?.emptyToNull() = this?.let { if (isEmpty()) null else this }
