package ch.ergon.arciphant.sample.customer.domain

import ch.ergon.arciphant.sample.customer.api.Customer
import ch.ergon.arciphant.sample.shared.base.CustomerId
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
