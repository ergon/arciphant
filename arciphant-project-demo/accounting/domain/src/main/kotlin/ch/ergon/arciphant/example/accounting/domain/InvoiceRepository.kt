package ch.ergon.arciphant.example.accounting.domain

import ch.ergon.arciphant.example.accounting.api.InvoiceId

interface InvoiceRepository {

    fun addInvoice(invoice: Invoice)

    fun getInvoice(id: InvoiceId): Invoice

}
