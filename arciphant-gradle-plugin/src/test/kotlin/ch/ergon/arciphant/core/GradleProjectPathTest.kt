package ch.ergon.arciphant.core

import ch.ergon.arciphant.util.dynamicTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows

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

    @Test
    fun `it should not allow instantiating a path with empty project names`() {

        val exception = assertThrows<IllegalArgumentException> {
            // copy method is used as a workaround to invoke private constructor directly without factory method (since factory method would filter out empty names)
            GradleProjectPath.of().copy(projectNames = listOf("a", "", "b"))
        }

        assertThat(exception.message).isEqualTo("Cannot create ${GradleProjectPath::class.simpleName} with empty project names: [a, , b]")
    }

    private fun GradleProjectPath.Companion.of(vararg projectNames: String) = GradleProjectPath.of(projectNames.toList())

}
