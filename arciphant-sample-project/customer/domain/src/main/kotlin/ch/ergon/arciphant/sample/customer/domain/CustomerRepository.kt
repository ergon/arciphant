package ch.ergon.arciphant.sample.customer.domain

import ch.ergon.arciphant.sample.customer.api.Customer
import ch.ergon.arciphant.sample.shared.base.CustomerId

interface CustomerRepository {

    fun insertCustomer(customer: Customer)

    fun loadCustomer(id: CustomerId): Customer

    fun fetchCustomers(): List<Customer>

}
