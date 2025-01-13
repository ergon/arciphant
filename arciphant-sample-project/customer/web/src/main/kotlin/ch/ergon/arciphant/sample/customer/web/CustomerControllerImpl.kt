package ch.ergon.arciphant.sample.customer.web

import ch.ergon.arciphant.sample.customer.api.Customer
import ch.ergon.arciphant.sample.customer.domain.CustomerService
import ch.ergon.arciphant.sample.customer.webapi.CustomerController
import ch.ergon.arciphant.sample.customer.webapi.CustomerDto
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomerControllerImpl(private val customerService: CustomerService) : CustomerController {

    override fun getCustomers(): List<CustomerDto> {
        return customerService.getCustomers().map { it.toApi() }
    }

    private fun Customer.toApi() = CustomerDto(id, firstname, lastname)
}
