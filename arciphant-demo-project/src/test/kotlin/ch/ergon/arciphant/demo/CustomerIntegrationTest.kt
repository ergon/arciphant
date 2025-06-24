package ch.ergon.arciphant.demo

import ch.ergon.arciphant.demo.customer.webapi.CustomerDto
import ch.ergon.arciphant.demo.shared.base.CustomerId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*

@SpringBootTest(webEnvironment = RANDOM_PORT)
class CustomerIntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `it should get customers`() {
        val customers = webTestClient.get()
            .uri("customers")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(CustomerDto::class.java)
            .returnResult()
            .body()

        assertThat(customers).containsExactlyInAnyOrder(
            CustomerDto(CustomerId(UUID.fromString("257ee41e-c989-4753-a5ff-e0ec45f6b609")), "Max", "Muster"),
            CustomerDto(CustomerId(UUID.fromString("9783cad4-2c83-4b7e-a82d-e827fb6d99e4")), "John", "Doe"),
        )
    }
}
