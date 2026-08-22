package ch.ergon.arciphant.example.accounting.db

import ch.ergon.arciphant.example.accounting.api.InvoiceId
import ch.ergon.arciphant.example.accounting.domain.Invoice
import ch.ergon.arciphant.example.accounting.domain.InvoiceFixtures
import ch.ergon.arciphant.example.shared.db.AbstractInMemoryRepositoryTest

class InvoiceRepositoryImplTest :
    AbstractInMemoryRepositoryTest<InvoiceRepositoryImpl, Invoice, InvoiceId>(
        repository = InvoiceRepositoryImpl(),
        addRecordFn = InvoiceRepositoryImpl::addInvoice,
        getRecordFn = InvoiceRepositoryImpl::getInvoice,
        record = InvoiceFixtures.anyInvoice,
        idFn = Invoice::id
    )
