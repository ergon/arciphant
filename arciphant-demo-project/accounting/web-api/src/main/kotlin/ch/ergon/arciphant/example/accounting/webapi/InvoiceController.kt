package ch.ergon.arciphant.example.accounting.webapi

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*

@RequestMapping("invoices")
interface InvoiceController {

    @PostMapping("{invoiceId}/payment")
    fun payInvoice(@PathVariable invoiceId: UUID)

}
