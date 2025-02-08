package ch.ergon.arciphant.model

internal interface ModuleRepository {
    fun create(): List<Module>
}