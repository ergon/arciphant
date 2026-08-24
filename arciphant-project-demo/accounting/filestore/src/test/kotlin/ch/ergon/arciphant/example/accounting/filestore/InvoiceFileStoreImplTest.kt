package ch.ergon.arciphant.example.accounting.filestore

import ch.ergon.arciphant.example.accounting.api.InvoiceId
import ch.ergon.arciphant.example.accounting.api.InvoiceIdFixtures
import ch.ergon.arciphant.example.shared.filestore.AbstractFileStoreTest

class InvoiceFileStoreImplTest : AbstractFileStoreTest<InvoiceFileStoreImpl, InvoiceId>(
    InvoiceFileStoreImpl(),
    InvoiceFileStoreImpl::persistInvoiceDocument,
    InvoiceFileStoreImpl::getInvoiceDocument,
    InvoiceIdFixtures.any,
)
