package ch.ergon.arciphant.sample.order.webapi

import ch.ergon.arciphant.sample.shared.base.CustomerId
import ch.ergon.arciphant.sample.shared.base.OrderId

data class OrderDto(
    val id: OrderId,
    val customerId: CustomerId,
    val amount: Double,
)

data class CreateOrderDto(
    val customerId: CustomerId,
    val amount: Double,
)
