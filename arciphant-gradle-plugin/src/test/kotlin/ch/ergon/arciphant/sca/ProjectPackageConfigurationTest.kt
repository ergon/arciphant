package ch.ergon.arciphant.sca

import ch.ergon.arciphant.dsl.ArciphantProjectDsl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProjectPackageConfigurationTest {

    @Test
    fun `it should accept a configuration without values`() {
        ArciphantProjectDsl().verify(":some:project")
    }

    @Test
    fun `it should accept an empty package name`() {
        ArciphantProjectDsl().apply { packageName = "" }.verify(":some:project")
    }

    @Test
    fun `it should reject a configuration with both package name and absolute package name`() {
        val dsl = ArciphantProjectDsl().apply {
            packageName = "a"
            absolutePackageName = "b"
        }

        val exception = assertThrows<IllegalArgumentException> { dsl.verify(":some:project") }

        assertThat(exception.message)
            .contains("Arciphant configuration error")
            .contains("project ':some:project' must not configure both 'packageName' and 'absolutePackageName'")
    }

    @Test
    fun `it should reject a package name containing whitespaces`() {
        val dsl = ArciphantProjectDsl().apply { packageName = "with whitespace" }

        val exception = assertThrows<IllegalArgumentException> { dsl.verify(":some:project") }

        assertThat(exception.message).contains("package name must not contain whitespaces")
    }

    @Test
    fun `it should reject an empty absolute package name`() {
        val dsl = ArciphantProjectDsl().apply { absolutePackageName = "" }

        val exception = assertThrows<IllegalArgumentException> { dsl.verify(":some:project") }

        assertThat(exception.message).contains("absolute package name must not be empty")
    }

}
