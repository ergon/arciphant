package ch.ergon.arciphant.demo.order.db

import ch.ergon.arciphant.demo.order.domain.Order
import ch.ergon.arciphant.demo.order.domain.OrderRepository
import ch.ergon.arciphant.demo.shared.base.OrderId
import org.springframework.stereotype.Repository

@Repository
class OrderRepositoryImpl : OrderRepository {

    companion object {
        private val orders = mutableListOf<Order>()
    }

    override fun add(order: Order) {
        orders.add(order)
    }

    override fun fetch(): List<Order> {
        return orders
    }

    override fun fetch(id: OrderId): Order {
        return orders.first { it.id == id }
    }
}
