package ch.ergon.arciphant.example.accounting.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.mockStatic
import java.time.LocalDate

class InvoiceTest {

    private val localDateMock = mockStatic(LocalDate::class.java, CALLS_REAL_METHODS)

    @AfterEach
    fun resetLocalDateMock() {
        localDateMock.close()
    }

    @Test
    fun `invoice should be overdue if due date is in past`() {
        val invoice = InvoiceFixtures.anyInvoice
        localDateMock.`when`<LocalDate>(LocalDate::now).thenReturn(invoice.dueDate.plusDays(1))

        assertThat(invoice.isOverdue()).isTrue()

    }

    @Test
    fun `invoice should NOT be overdue if due date is current date`() {
        val invoice = InvoiceFixtures.anyInvoice
        localDateMock.`when`<LocalDate>(LocalDate::now).thenReturn(invoice.dueDate)

        assertThat(invoice.isOverdue()).isFalse()
    }

    @Test
    fun `invoice should NOT be overdue if due date is in future`() {
        val invoice = InvoiceFixtures.anyInvoice
        localDateMock.`when`<LocalDate>(LocalDate::now).thenReturn(invoice.dueDate.minusDays(1))

        assertThat(invoice.isOverdue()).isFalse()
    }

}
