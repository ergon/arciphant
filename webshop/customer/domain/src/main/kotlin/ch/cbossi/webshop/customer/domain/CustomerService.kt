package ch.cbossi.webshop.customer.domain

import ch.cbossi.webshop.customer.api.Customer
import ch.cbossi.webshop.shared.api.CustomerId
import org.springframework.stereotype.Service

@Service
class CustomerService(private val repository: CustomerRepository) {

    fun createCustomer(customer: Customer) {
        repository.insertCustomer(customer)
    }

    fun loadCustomer(id: CustomerId): Customer {
        return repository.loadCustomer(id)
    }

}
