package ch.ergon.arciphant.sample.order.domain

import ch.ergon.arciphant.sample.shared.base.CustomerId
import ch.ergon.arciphant.sample.shared.base.OrderId

data class Order(val id: OrderId, val customerId: CustomerId, val amount: Double)
