package ch.ergon.arciphant.demo.customer.domain

import ch.ergon.arciphant.demo.customer.api.Customer
import ch.ergon.arciphant.demo.shared.base.CustomerId

interface CustomerRepository {

    fun insertCustomer(customer: Customer)

    fun loadCustomer(id: CustomerId): Customer

    fun fetchCustomers(): List<Customer>

}
