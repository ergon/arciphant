package ch.cbossi.webshop.customer.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CustomerTest {

    @Test
    fun testCustomer() {
        val customer = Customer("Max", "Muster")

        assertThat(customer.name).isEqualTo("Max Muster")
    }
}
