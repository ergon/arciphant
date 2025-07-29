package ch.ergon.arciphant.example.shared.domain

import ch.ergon.arciphant.example.shared.api.DisciplineIdFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DisciplineTest {

    @Test
    fun `it should throw exception if title is blank`() {
        val exception = assertThrows<IllegalArgumentException> {
            Discipline(DisciplineIdFixtures.any, "")
        }

        assertThat(exception.message).isEqualTo("Discipline title must not be blank")
    }

}
