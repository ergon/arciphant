package ch.cbossi.webshop.customer.db

import ch.cbossi.webshop.customer.api.CustomerFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CustomerRepositoryImplTest {

    private val repository = CustomerRepositoryImpl()

    @Test
    fun test() {
        repository.insertCustomer(CustomerFixtures.customer)

        val customer = repository.loadCustomer()

        assertThat(customer).isEqualTo(CustomerFixtures.customer)
    }

}