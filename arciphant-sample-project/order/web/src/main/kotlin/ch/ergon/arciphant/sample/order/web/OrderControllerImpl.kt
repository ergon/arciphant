package ch.ergon.arciphant.sample.order.web

import ch.ergon.arciphant.sample.order.domain.Order
import ch.ergon.arciphant.sample.order.domain.OrderService
import ch.ergon.arciphant.sample.order.webapi.CreateOrderDto
import ch.ergon.arciphant.sample.order.webapi.OrderController
import ch.ergon.arciphant.sample.order.webapi.OrderDto
import ch.ergon.arciphant.sample.shared.base.CustomerId
import ch.ergon.arciphant.sample.shared.base.OrderId
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class OrderControllerImpl(private val orderService: OrderService) : OrderController {
    override fun createOrder(order: CreateOrderDto): OrderDto {
        return orderService.createOrder(CustomerId(order.customerId), order.amount).toApi()
    }

    override fun getOrders(): List<OrderDto> {
        return orderService.getOrders().map { it.toApi() }
    }

    override fun getOrder(orderId: UUID): OrderDto {
        return orderService.getOrder(OrderId(orderId)).toApi()
    }

    private fun Order.toApi() = OrderDto(id.value, customerId.value, amount)
}
