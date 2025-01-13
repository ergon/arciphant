package ch.ergon.arciphant.sample.customer.webapi

import ch.ergon.arciphant.sample.shared.base.CustomerId


data class CustomerDto(
    val id: CustomerId,
    val firstName: String,
    val lastName: String,
)
