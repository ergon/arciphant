package ch.ergon.arciphant.example.shared.db

import ch.ergon.arciphant.example.shared.api.Id

abstract class InMemoryRepository<R, ID : Id> {

    private val recordsById = mutableMapOf<ID, R>()

    protected fun addRecord(id: ID, record: R) = recordsById.put(id, record)

    protected fun getRecords() = recordsById.values.toList()

    protected fun getRecord(id: ID) = recordsById.getValue(id)

    protected fun deleteRecord(id: ID) = recordsById.remove(id)

}
