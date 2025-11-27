package ch.ergon.arciphant.core

import ch.ergon.arciphant.util.dynamicTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class GradleProjectPathTest {

    @TestFactory
    fun testValue() = dynamicTest(
        GradleProjectPath.of() to ":",
        GradleProjectPath.of("module") to ":module",
        GradleProjectPath.of("module", "component") to ":module:component",
        GradleProjectPath.of("any", "nested", "project", "structure") to ":any:nested:project:structure",
    ) { it.value }

    @Test
    fun `factory method should filter out empty project names`() {
        val projectPath = GradleProjectPath.of("a", "", "b")

        assertThat(projectPath.value).isEqualTo(":a:b")
    }

    private fun GradleProjectPath.Companion.of(vararg projectNames: String) = GradleProjectPath.of(projectNames.toList())

}
