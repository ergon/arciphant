package ch.ergon.arciphant.example.accounting.domain

import ch.ergon.arciphant.example.accounting.api.InvoiceIdFixtures
import java.math.BigDecimal
import java.time.LocalDate

object InvoiceFixtures {

    val anyInvoice = Invoice(
        id = InvoiceIdFixtures.any,
        amount = BigDecimal("99.90"),
        dueDate = LocalDate.of(2025, 12, 31)
    )

}
