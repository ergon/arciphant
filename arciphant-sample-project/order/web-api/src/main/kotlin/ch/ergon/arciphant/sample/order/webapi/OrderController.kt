package ch.ergon.arciphant.sample.order.webapi

import ch.ergon.arciphant.sample.shared.base.OrderId
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RequestMapping("orders")
interface OrderController {

    @PostMapping
    fun createOrder(@RequestBody order: CreateOrderDto): OrderDto

    @GetMapping
    fun getOrders(): List<OrderDto>

    @GetMapping("{orderId}")
    fun getOrder(@PathVariable orderId: UUID): OrderDto

}
