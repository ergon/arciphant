package ch.ergon.arciphant.dsl

internal fun String?.emptyToNull() = this?.let { ifEmpty { null } }
