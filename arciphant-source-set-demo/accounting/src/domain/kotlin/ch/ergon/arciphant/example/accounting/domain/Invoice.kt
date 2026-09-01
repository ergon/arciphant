package ch.ergon.arciphant.example.accounting.domain

import ch.ergon.arciphant.example.accounting.api.InvoiceId
import java.math.BigDecimal
import java.time.LocalDate

data class Invoice(val id: InvoiceId, val amount: BigDecimal, val dueDate: LocalDate) {

    fun isOverdue(): Boolean = dueDate.isBefore(LocalDate.now())

}
