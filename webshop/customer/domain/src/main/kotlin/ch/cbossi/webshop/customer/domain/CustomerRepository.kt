package ch.cbossi.webshop.customer.domain

import ch.cbossi.webshop.customer.api.Customer

interface CustomerRepository {

    fun insertCustomer(customer: Customer)

    fun loadCustomer(): Customer

}