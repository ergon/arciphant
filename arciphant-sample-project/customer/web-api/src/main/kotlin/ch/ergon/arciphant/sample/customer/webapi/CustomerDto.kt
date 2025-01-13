package ch.ergon.arciphant.sample.customer.webapi

import java.util.*

data class CustomerDto(
    val id: UUID,
    val firstName: String,
    val lastName: String,
)
