package ch.ergon.arciphant.demo.customer.db

import ch.ergon.arciphant.demo.customer.api.CustomerFixtures
import ch.ergon.arciphant.demo.shared.base.IdFixtures
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
