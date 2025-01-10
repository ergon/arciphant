package ch.ergon.arciphant.sample.customer.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CustomerTest {

    @Test
    fun testName() {
        val customer = CustomerFixtures.customer

        assertThat(customer.name).isEqualTo("Max Muster")
    }
}
