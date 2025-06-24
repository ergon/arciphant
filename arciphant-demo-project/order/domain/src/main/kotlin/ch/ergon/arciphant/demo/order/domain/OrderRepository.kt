package ch.ergon.arciphant.demo.order.domain

import ch.ergon.arciphant.demo.shared.base.OrderId

interface OrderRepository {

    fun add(order: Order)

    fun fetch(): List<Order>

    fun fetch(id: OrderId): Order

}
