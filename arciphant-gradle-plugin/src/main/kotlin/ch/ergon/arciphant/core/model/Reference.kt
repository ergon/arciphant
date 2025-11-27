package ch.ergon.arciphant.core.model

data class ModuleReference(val parentProjectPath: List<String> = emptyList(), val name: String) {
    val path by lazy { parentProjectPath + name }
}

data class ComponentReference internal constructor(val name: String)
