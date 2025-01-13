package ch.ergon.arciphant.sample

import ch.ergon.arciphant.sample.order.webapi.CreateOrderDto
import ch.ergon.arciphant.sample.order.webapi.OrderDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*

@SpringBootTest(webEnvironment = RANDOM_PORT)
class OrderIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `test orders`() {
        val customerId = UUID.randomUUID()
        val amount = 42.5
        val createdOrder = webTestClient.post()
            .uri("orders")
            .bodyValue(CreateOrderDto(customerId, amount))
            .exchange()
            .expectStatus().isOk
            .expectBody(OrderDto::class.java)
            .returnResult()
            .body()

        assertThat(createdOrder.customerId).isEqualTo(customerId)
        assertThat(createdOrder.amount).isEqualTo(amount)

        val orders = webTestClient.get()
            .uri("orders")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(OrderDto::class.java)
            .returnResult()
            .body()

        assertThat(orders).containsExactlyInAnyOrder(
            OrderDto(createdOrder.id, customerId, amount),
        )

        val order = webTestClient.get()
            .uri("orders/${createdOrder.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody(OrderDto::class.java)
            .returnResult()
            .body()

        assertThat(order).isEqualTo(OrderDto(createdOrder.id, customerId, amount))
    }
}
