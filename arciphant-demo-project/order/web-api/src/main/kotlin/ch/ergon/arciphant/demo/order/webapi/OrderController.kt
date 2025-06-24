package ch.ergon.arciphant.demo.order.webapi

import ch.ergon.arciphant.demo.shared.base.OrderId
import org.springframework.web.bind.annotation.*

@RequestMapping("orders")
interface OrderController {

    @PostMapping
    fun createOrder(@RequestBody order: CreateOrderDto): OrderDto

    @GetMapping
    fun getOrders(): List<OrderDto>

    @GetMapping("{orderId}")
    fun getOrder(@PathVariable orderId: OrderId): OrderDto

}
