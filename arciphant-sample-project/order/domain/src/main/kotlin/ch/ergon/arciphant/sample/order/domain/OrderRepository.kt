package ch.ergon.arciphant.sample.order.domain

import ch.ergon.arciphant.sample.shared.base.OrderId

interface OrderRepository {

    fun add(order: Order)

    fun fetch(): List<Order>

    fun fetch(id: OrderId): Order

}
