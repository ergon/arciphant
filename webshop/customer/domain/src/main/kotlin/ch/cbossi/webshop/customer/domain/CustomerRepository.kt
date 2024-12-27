package ch.cbossi.webshop.customer.domain

import ch.cbossi.webshop.customer.api.Customer
import ch.cbossi.webshop.shared.api.CustomerId

interface CustomerRepository {

    fun insertCustomer(customer: Customer)

    fun loadCustomer(id: CustomerId): Customer

}