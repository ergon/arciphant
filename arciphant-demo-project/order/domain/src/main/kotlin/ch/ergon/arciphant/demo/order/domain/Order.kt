package ch.ergon.arciphant.demo.order.domain

import ch.ergon.arciphant.demo.shared.base.CustomerId
import ch.ergon.arciphant.demo.shared.base.OrderId

data class Order(val id: OrderId, val customerId: CustomerId, val amount: Double)
