package ch.ergon.arciphant.example.accounting.db

import ch.ergon.arciphant.example.accounting.api.InvoiceId
import ch.ergon.arciphant.example.accounting.domain.Invoice
import ch.ergon.arciphant.example.accounting.domain.InvoiceRepository
import ch.ergon.arciphant.example.shared.db.InMemoryRepository
import org.springframework.stereotype.Repository

@Repository
class InvoiceRepositoryImpl : InvoiceRepository, InMemoryRepository<Invoice, InvoiceId>() {

    override fun addInvoice(invoice: Invoice) {
        addRecord(invoice.id, invoice)
    }

    override fun getInvoice(id: InvoiceId): Invoice {
        return getRecord(id)
    }


}
