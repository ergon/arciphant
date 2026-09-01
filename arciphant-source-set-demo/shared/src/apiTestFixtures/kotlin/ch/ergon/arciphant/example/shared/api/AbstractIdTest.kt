package ch.ergon.arciphant.example.shared.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

abstract class AbstractIdTest<ID : Id>(private val idFixtures: AbstractIdFixtures<ID>) {

    @Test
    fun `ID should have proper string representation`() {
        val id = idFixtures.any
        assertThat(id.toString()).isEqualTo("${id::class.simpleName}(value=${id.value})")
    }

    @Test
    fun `equals of ID should compare by value`() {
        val uuid = UUID.randomUUID()
        val id1 = idFixtures.of(uuid)
        val id2 = idFixtures.of(uuid)

        assertThat(id1 == id2).isTrue()
    }

}
