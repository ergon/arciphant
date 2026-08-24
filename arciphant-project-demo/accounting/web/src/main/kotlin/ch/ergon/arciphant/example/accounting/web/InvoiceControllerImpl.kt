package ch.ergon.arciphant.example.accounting.web

import ch.ergon.arciphant.example.accounting.api.InvoiceId
import ch.ergon.arciphant.example.accounting.domain.InvoiceRepository
import ch.ergon.arciphant.example.accounting.ppa.PaymentProvider
import ch.ergon.arciphant.example.accounting.webapi.InvoiceController
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class InvoiceControllerImpl(
    private val invoiceRepository: InvoiceRepository,
    private val paymentProvider: PaymentProvider,
) : InvoiceController {

    override fun payInvoice(invoiceId: UUID) {
        val invoice = invoiceRepository.getInvoice(InvoiceId(invoiceId))
        paymentProvider.payInvoice(invoice.amount)
    }


}
