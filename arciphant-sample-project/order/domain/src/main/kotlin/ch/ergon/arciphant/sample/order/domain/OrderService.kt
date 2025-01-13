package ch.ergon.arciphant.sample.order.domain

import ch.ergon.arciphant.sample.shared.base.CustomerId
import ch.ergon.arciphant.sample.shared.base.OrderId
import org.springframework.stereotype.Service
import java.util.*

@Service
class OrderService(private val repository: OrderRepository) {

    fun createOrder(customerId: CustomerId, amount: Double): Order {
        val order = Order(OrderId(UUID.randomUUID()), customerId, amount)
        repository.add(order)
        return order
    }

    fun getOrders(): List<Order> {
        return repository.fetch()
    }

    fun getOrder(id: OrderId): Order {
        return repository.fetch(id)
    }

}
