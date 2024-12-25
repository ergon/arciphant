package ch.cbossi.webshop.customer.domain

import ch.cbossi.webshop.customer.api.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CustomerServiceTest {

    private val repositoryMock = mock<CustomerRepository>()
    private val service = CustomerService(repositoryMock)

    @Test
    fun testCreateCustomer() {
        val customer = Customer("Max", "Muster")

        service.createCustomer(customer)

        verify(repositoryMock).insertCustomer(customer)
    }

    @Test
    fun testLoadCustomer() {
        whenever(repositoryMock.loadCustomer()).thenReturn(Customer("Max", "Muster"))

        val customer = service.loadCustomer()

        assertThat(customer).isEqualTo(Customer("Max", "Muster"))
    }
}