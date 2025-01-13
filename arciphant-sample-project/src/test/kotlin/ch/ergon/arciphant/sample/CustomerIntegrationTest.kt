package ch.ergon.arciphant.sample

import ch.ergon.arciphant.sample.customer.webapi.CustomerDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.reactive.server.WebTestClient

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
            CustomerDto("Max", "Muster"),
            CustomerDto("John", "Doe"),
        )
    }
}
