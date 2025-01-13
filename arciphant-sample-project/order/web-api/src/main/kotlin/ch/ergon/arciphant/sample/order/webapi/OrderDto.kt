package ch.ergon.arciphant.sample.order.webapi

import java.util.*

data class OrderDto(
    val id: UUID,
    val customerId: UUID,
    val amount: Double,
)

data class CreateOrderDto(
    val customerId: UUID,
    val amount: Double,
)
