package ch.ergon.arciphant.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CollectionExtensionTest {

    @Nested
    inner class MergeTest {

        @Test
        fun `it should merge all entries of maps with distinct keys`() {
            val map1 = mapOf(
                "a" to "A",
                "b" to "B"
            )
            val map2 = mapOf(
                "c" to "c",
                "d" to "D"
            )
            val map3 = mapOf(
                "e" to "E",
                "f" to "F"
            )

            val result = listOf(map1, map2, map3).merge()

            assertThat(result).isEqualTo(
                mapOf(
                    "a" to "A",
                    "b" to "B",
                    "c" to "c",
                    "d" to "D",
                    "e" to "E",
                    "f" to "F"
                )
            )
        }

        @Test
        fun `values of maps further back in the input list should override values from maps further ahead`() {
            val map1 = mapOf(
                "a" to "A",
                "b" to "B",
                "c" to "C",
                "d" to "D"
            )
            val map2 = mapOf(
                "b" to "BB",
                "e" to "EE",
                "f" to "FF"
            )
            val map3 = mapOf(
                "a" to "AAA",
                "c" to "CCC",
                "e" to "EEE",
                "g" to "GGG"
            )

            val result = listOf(map1, map2, map3).merge()

            assertThat(result).isEqualTo(
                mapOf(
                    "a" to "AAA",
                    "b" to "BB",
                    "c" to "CCC",
                    "d" to "D",
                    "e" to "EEE",
                    "f" to "FF",
                    "g" to "GGG"
                )
            )
        }

        @Test
        fun `it should return an empty map if input list is empty`() {

        }

    }

}