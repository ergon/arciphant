package ch.cbossi.webshop.customer.domain

import ch.cbossi.webshop.customer.api.Customer
import org.springframework.stereotype.Service

@Service
class CustomerService {

    fun createCustomer() {
        val c = Customer("John", "Doe")
    }

}
