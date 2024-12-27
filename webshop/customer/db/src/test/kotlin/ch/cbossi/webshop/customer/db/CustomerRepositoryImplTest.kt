package ch.cbossi.webshop.customer.db

import ch.cbossi.webshop.customer.api.CustomerFixtures
import ch.cbossi.webshop.shared.api.IdFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CustomerRepositoryImplTest {

    private val repository = CustomerRepositoryImpl()

    @Test
    fun test() {
        repository.insertCustomer(CustomerFixtures.customer)

        val customer = repository.loadCustomer(IdFixtures.customerId)

        assertThat(customer).isEqualTo(CustomerFixtures.customer)
    }

}