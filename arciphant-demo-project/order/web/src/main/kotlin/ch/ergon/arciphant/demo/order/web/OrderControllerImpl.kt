package ch.ergon.arciphant.demo.order.web

import ch.ergon.arciphant.demo.order.domain.Order
import ch.ergon.arciphant.demo.order.domain.OrderService
import ch.ergon.arciphant.demo.order.webapi.CreateOrderDto
import ch.ergon.arciphant.demo.order.webapi.OrderController
import ch.ergon.arciphant.demo.order.webapi.OrderDto
import ch.ergon.arciphant.demo.shared.base.OrderId
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderControllerImpl(private val orderService: OrderService) : OrderController {
    override fun createOrder(order: CreateOrderDto): OrderDto {
        return orderService.createOrder(order.customerId, order.amount).toApi()
    }

    override fun getOrders(): List<OrderDto> {
        return orderService.getOrders().map { it.toApi() }
    }

    override fun getOrder(orderId: OrderId): OrderDto {
        return orderService.getOrder(orderId).toApi()
    }

    private fun Order.toApi() = OrderDto(id, customerId, amount)
}
