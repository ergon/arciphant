package ch.ergon.arciphant.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest.dynamicTest

fun <I, O> dynamicTest(
    vararg testCases: Pair<I, O>,
    displayNameFn: (I, O) -> String = { input, output -> "$input -> $output" },
    testMethod: (I) -> O,
) = testCases.map { (input, expectedOutput) ->
    val displayName = displayNameFn(input, expectedOutput)
    dynamicTest(displayName) {
        val actualOutput = testMethod(input)
        assertThat(actualOutput).isEqualTo(expectedOutput)
    }
}
