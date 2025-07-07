package ch.ergon.arciphant.dsl

fun verifyName(name: String, typeAsString: String) {
    verifyConfiguration(!name.contains(" ")) { "$typeAsString name must NOT contain whitespaces" }
    verifyConfiguration(name.isNotEmpty()) { "$typeAsString name must NOT be empty" }
}

private fun verifyConfiguration(condition: Boolean, lazyMessage: () -> String) =
    require(condition) { "Arciphant configuration error: ${lazyMessage()}" }
