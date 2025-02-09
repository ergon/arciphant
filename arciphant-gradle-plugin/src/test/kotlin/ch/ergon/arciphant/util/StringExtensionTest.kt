package ch.ergon.arciphant.util

import ch.ergon.arciphant.core.dynamicTest
import org.junit.jupiter.api.TestFactory

class StringExtensionTest {

    @TestFactory
    fun testEmptyToNull() = dynamicTest(
        "" to null,
        "any" to "any",
        " " to " ",
        "   " to "   ",
    ) { it.emptyToNull() }
}