package ch.ergon.arciphant.demo.customer.web

import ch.ergon.arciphant.demo.customer.api.Customer
import ch.ergon.arciphant.demo.customer.domain.CustomerService
import ch.ergon.arciphant.demo.customer.webapi.CustomerController
import ch.ergon.arciphant.demo.customer.webapi.CustomerDto
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomerControllerImpl(private val customerService: CustomerService) : CustomerController {

    override fun getCustomers(): List<CustomerDto> {
        return customerService.getCustomers().map { it.toApi() }
    }

    private fun Customer.toApi() = CustomerDto(id, firstname, lastname)
}
