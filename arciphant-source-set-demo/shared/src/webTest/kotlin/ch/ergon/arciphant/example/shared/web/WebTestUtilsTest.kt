package ch.ergon.arciphant.example.shared.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WebTestUtilsTest {

    @Test
    fun `it should deserialize list`() {
        val serialized = """[{"name":"test1"},{"name":"test2"}]"""

        val objects = serialized.deserializeList(TestObject::class)

        assertThat(objects).containsExactlyInAnyOrder(
            TestObject("test1"),
            TestObject("test2"),
        )
    }

}

data class TestObject(val name: String)
