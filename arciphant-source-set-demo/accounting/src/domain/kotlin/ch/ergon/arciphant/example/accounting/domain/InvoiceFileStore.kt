package ch.ergon.arciphant.example.accounting.domain

import ch.ergon.arciphant.example.accounting.api.InvoiceId
import java.io.File

interface InvoiceFileStore {

    fun persistInvoiceDocument(invoiceId: InvoiceId, invoiceDocument: File)
    fun getInvoiceDocument(invoiceId: InvoiceId): File
}
