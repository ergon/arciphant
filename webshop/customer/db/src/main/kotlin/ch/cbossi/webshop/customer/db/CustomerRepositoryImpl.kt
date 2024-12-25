package ch.cbossi.webshop.customer.db

import ch.cbossi.webshop.customer.api.Customer
import ch.cbossi.webshop.customer.domain.CustomerRepository

class CustomerRepositoryImpl : CustomerRepository {

    override fun loadCustomer(): Customer {
        return Customer("John", "Doe")
    }
}