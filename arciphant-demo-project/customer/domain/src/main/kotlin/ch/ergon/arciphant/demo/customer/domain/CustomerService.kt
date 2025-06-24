package ch.ergon.arciphant.demo.customer.domain

import ch.ergon.arciphant.demo.customer.api.Customer
import ch.ergon.arciphant.demo.shared.base.CustomerId
import org.springframework.stereotype.Service

@Service
class CustomerService(private val repository: CustomerRepository) {

    fun createCustomer(customer: Customer) {
        repository.insertCustomer(customer)
    }

    fun loadCustomer(id: CustomerId): Customer {
        return repository.loadCustomer(id)
    }

    fun getCustomers(): List<Customer> {
        return repository.fetchCustomers()
    }

}
