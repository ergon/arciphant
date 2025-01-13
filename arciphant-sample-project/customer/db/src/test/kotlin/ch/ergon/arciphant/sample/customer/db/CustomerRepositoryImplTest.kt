package ch.ergon.arciphant.sample.customer.db

import ch.ergon.arciphant.sample.customer.api.CustomerFixtures
import ch.ergon.arciphant.sample.shared.base.IdFixtures
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
