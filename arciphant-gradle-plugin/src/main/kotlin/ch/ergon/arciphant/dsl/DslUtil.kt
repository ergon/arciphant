package ch.ergon.arciphant.dsl

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun verifyName(name: String, type: String) {
    verify(!name.contains(" ")) { "$type name must not contain whitespaces" }
    verify(name.isNotEmpty()) { "$type name must not be empty" }
}

@OptIn(ExperimentalContracts::class)
fun verify(condition: Boolean, lazyMessage: () -> String) {
    contract {
        returns() implies condition
    }
    require(condition) { "Arciphant configuration error: ${lazyMessage()}" }
}
