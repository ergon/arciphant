package ch.ergon.arciphant.sample.customer.db

import ch.ergon.arciphant.sample.customer.api.Customer
import ch.ergon.arciphant.sample.customer.domain.CustomerRepository
import ch.ergon.arciphant.sample.shared.base.CustomerId
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CustomerRepositoryImpl : CustomerRepository {

    private var customer: Customer? = null

    override fun insertCustomer(customer: Customer) {
        this.customer = customer
    }

    override fun loadCustomer(id: CustomerId): Customer {
        return customer.let { if (it?.id == id) customer else null } ?: error("No customer found with id $id")
    }

    override fun fetchCustomers(): List<Customer> {
        return listOf(
            Customer(CustomerId(UUID.randomUUID()), "Max", "Muster"),
            Customer(CustomerId(UUID.randomUUID()), "John", "Doe"),
        )
    }
}
