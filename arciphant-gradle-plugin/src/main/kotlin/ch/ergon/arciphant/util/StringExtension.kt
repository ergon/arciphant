package ch.ergon.arciphant.util

internal fun String?.emptyToNull() = this?.let { ifEmpty { null } }
