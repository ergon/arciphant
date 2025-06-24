package ch.ergon.arciphant.demo.order.webapi

import ch.ergon.arciphant.demo.shared.base.CustomerId
import ch.ergon.arciphant.demo.shared.base.OrderId

data class OrderDto(
    val id: OrderId,
    val customerId: CustomerId,
    val amount: Double,
)

data class CreateOrderDto(
    val customerId: CustomerId,
    val amount: Double,
)
