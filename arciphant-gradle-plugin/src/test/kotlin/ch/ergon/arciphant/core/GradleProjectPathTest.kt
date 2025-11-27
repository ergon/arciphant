package ch.ergon.arciphant.core

import ch.ergon.arciphant.util.dynamicTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class GradleProjectPathTest {

    @TestFactory
    fun testValue() = dynamicTest(
        GradleProjectPath() to ":",
        GradleProjectPath("module") to ":module",
        GradleProjectPath("module", "component") to ":module:component",
        GradleProjectPath("any", "nested", "project", "structure") to ":any:nested:project:structure",
    ) { it.value }

    @Test
    fun `it should filter out empty segments`() {
        val projectPath = GradleProjectPath("a", "", "b")

        assertThat(projectPath.value).isEqualTo(":a:b")
    }

}
