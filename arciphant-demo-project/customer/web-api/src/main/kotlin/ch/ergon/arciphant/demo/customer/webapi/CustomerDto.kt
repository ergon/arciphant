package ch.ergon.arciphant.demo.customer.webapi

import ch.ergon.arciphant.demo.shared.base.CustomerId


data class CustomerDto(
    val id: CustomerId,
    val firstName: String,
    val lastName: String,
)
