package ch.ergon.arciphant.example.shared.db

import ch.ergon.arciphant.example.shared.api.Id
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

abstract class AbstractInMemoryRepositoryTest<IMR : InMemoryRepository<R, ID>, R, ID : Id>(
    protected val repository: IMR,
    private val addRecordFn: IMR.(R) -> Unit,
    private val getRecordFn: IMR.(ID) -> R,
    private val record: R,
    private val idFn: R.() -> ID,
) {

    @Test
    fun `it should persist record`() {
        val id = idFn(record)
        repository.addRecordFn(record)

        val recordFromDb = repository.getRecordFn(id)

        assertThat(recordFromDb).isEqualTo(record)

    }

}
