package ch.ergon.arciphant.example.accounting.web

import ch.ergon.arciphant.example.accounting.api.InvoiceIdFixtures
import ch.ergon.arciphant.example.accounting.domain.InvoiceFixtures
import ch.ergon.arciphant.example.accounting.domain.InvoiceRepository
import ch.ergon.arciphant.example.accounting.ppa.PaymentProvider
import ch.ergon.arciphant.example.shared.web.IsolatedWebMvcTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post


@IsolatedWebMvcTest(InvoiceControllerImpl::class)
class InvoiceControllerImplTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var invoiceRepositoryMock: InvoiceRepository

    @MockitoBean
    private lateinit var paymentProviderMock: PaymentProvider

    @Test
    fun `it should register endpoint to pay invoices`() {
        val invoiceId = InvoiceIdFixtures.any
        whenever(invoiceRepositoryMock.getInvoice(invoiceId)).thenReturn(InvoiceFixtures.anyInvoice)

        mockMvc.post("/invoices/${invoiceId.value}/payment")
            .andExpect { status { isOk() } }
    }

}
