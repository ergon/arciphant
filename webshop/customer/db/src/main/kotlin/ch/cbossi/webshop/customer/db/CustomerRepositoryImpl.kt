package ch.cbossi.webshop.customer.db

import ch.cbossi.webshop.customer.api.Customer
import ch.cbossi.webshop.customer.domain.CustomerRepository

class CustomerRepositoryImpl : CustomerRepository {

    private var customer: Customer? = null

    override fun insertCustomer(customer: Customer) {
        this.customer = customer
    }

    override fun loadCustomer(): Customer {
        return customer ?: error("No customer found")
    }
}