package ch.ergon.arciphant.example.accounting.filestore

import ch.ergon.arciphant.example.accounting.api.InvoiceId
import ch.ergon.arciphant.example.accounting.domain.InvoiceFileStore
import ch.ergon.arciphant.example.shared.filestore.FileStore
import org.springframework.stereotype.Service
import java.io.File

@Service
class InvoiceFileStoreImpl : InvoiceFileStore, FileStore() {

    override fun persistInvoiceDocument(invoiceId: InvoiceId, invoiceDocument: File) {
        addFile(invoiceId, invoiceDocument)
    }

    override fun getInvoiceDocument(invoiceId: InvoiceId): File {
        return getFile(invoiceId)
    }
}
