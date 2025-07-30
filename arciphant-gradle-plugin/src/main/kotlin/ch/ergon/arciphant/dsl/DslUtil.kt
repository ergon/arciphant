package ch.ergon.arciphant.dsl

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun verifyName(name: String, typeAsString: String) {
    verify(!name.contains(" ")) { "$typeAsString name must NOT contain whitespaces" }
    verify(name.isNotEmpty()) { "$typeAsString name must NOT be empty" }
}

@OptIn(ExperimentalContracts::class)
fun verify(condition: Boolean, lazyMessage: () -> String) {
    contract {
        returns() implies condition
    }
    require(condition) { "Arciphant configuration error: ${lazyMessage()}" }
}
