package ch.ergon.arciphant.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun verifyName(name: String, type: String, forbidEmpty: Boolean = false) {
    verify(!name.contains(" ")) { "$type name must not contain whitespaces" }
    verify(!forbidEmpty || name.isNotEmpty()) { "$type name must not be empty" }
}

@OptIn(ExperimentalContracts::class)
fun verify(condition: Boolean, lazyMessage: () -> String) {
    contract {
        returns() implies condition
    }
    require(condition) { "Arciphant configuration error: ${lazyMessage()}" }
}
