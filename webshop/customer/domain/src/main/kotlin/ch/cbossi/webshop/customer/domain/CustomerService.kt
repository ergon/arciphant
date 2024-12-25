package ch.cbossi.webshop.customer.domain

import ch.cbossi.webshop.customer.api.Customer
import org.springframework.stereotype.Service

@Service
class CustomerService(private val repository: CustomerRepository) {

    fun createCustomer(customer: Customer) {
        repository.insertCustomer(customer)
    }

    fun loadCustomer(): Customer {
        return repository.loadCustomer()
    }

}
