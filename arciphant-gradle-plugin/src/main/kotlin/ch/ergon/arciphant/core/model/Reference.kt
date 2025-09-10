package ch.ergon.arciphant.core.model

data class ModuleReference(val name: String)

data class ComponentReference internal constructor(val name: String) {
    fun hasName(): Boolean = name.isNotEmpty()
}
