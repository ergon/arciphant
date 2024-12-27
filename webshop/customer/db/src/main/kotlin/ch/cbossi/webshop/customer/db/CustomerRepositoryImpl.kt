package ch.cbossi.webshop.customer.db

import ch.cbossi.webshop.customer.api.Customer
import ch.cbossi.webshop.customer.domain.CustomerRepository
import ch.cbossi.webshop.shared.api.CustomerId

class CustomerRepositoryImpl : CustomerRepository {

    private var customer: Customer? = null

    override fun insertCustomer(customer: Customer) {
        this.customer = customer
    }

    override fun loadCustomer(id: CustomerId): Customer {
        return customer.let { if (it?.id == id) customer else null } ?: error("No customer found with id $id")
    }
}