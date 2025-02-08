package ch.ergon.arciphant.model

internal interface ModuleRepository {
    fun load(): List<Module>
}