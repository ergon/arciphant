package ch.cbossi.webshop.customer.db

import ch.cbossi.webshop.customer.api.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CustomerRepositoryImplTest {

    private val repository = CustomerRepositoryImpl()

    @Test
    fun test() {
        repository.insertCustomer(Customer("John", "Doe"))

        val customer = repository.loadCustomer()

        assertThat(customer).isEqualTo(Customer("John", "Doe"))
    }

}