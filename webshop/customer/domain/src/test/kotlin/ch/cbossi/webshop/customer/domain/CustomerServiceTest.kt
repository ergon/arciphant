package ch.cbossi.webshop.customer.domain

import ch.cbossi.webshop.customer.api.Customer
import ch.cbossi.webshop.customer.api.CustomerFixtures
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
        val customer = CustomerFixtures.customer

        service.createCustomer(customer)

        verify(repositoryMock).insertCustomer(customer)
    }

    @Test
    fun testLoadCustomer() {
        whenever(repositoryMock.loadCustomer()).thenReturn(CustomerFixtures.customer)

        val customer = service.loadCustomer()

        assertThat(customer).isEqualTo(CustomerFixtures.customer)
    }
}