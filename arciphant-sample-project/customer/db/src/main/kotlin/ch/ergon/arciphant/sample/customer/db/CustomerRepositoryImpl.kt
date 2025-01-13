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
            Customer(CustomerId(UUID.fromString("257ee41e-c989-4753-a5ff-e0ec45f6b609")), "Max", "Muster"),
            Customer(CustomerId(UUID.fromString("9783cad4-2c83-4b7e-a82d-e827fb6d99e4")), "John", "Doe"),
        )
    }
}
