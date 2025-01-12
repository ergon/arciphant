package ch.ergon.arciphant.sample.customer.webapi

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("customers")
interface CustomerController {

    @GetMapping
    fun getCustomers(): List<CustomerDto>

}