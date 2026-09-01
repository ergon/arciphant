package ch.ergon.arciphant.core

import ch.ergon.arciphant.dsl.ArciphantDsl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class GlobalSettingsRepositoryTest {

    @Test
    fun `it should use project layout by default`() {
        val settings = GlobalSettingsRepository(ArciphantDsl()).load()

        assertThat(settings.componentLayout).isEqualTo(ComponentLayout.PROJECT)
    }

    @Test
    fun `it should use source set defaults`() {
        val dsl = ArciphantDsl().apply { sourceSetComponentLayout() }

        val settings = GlobalSettingsRepository(dsl).load()

        assertThat(settings.sourceSetComponentSettings.withTestSourceSet).isTrue()
        assertThat(settings.sourceSetComponentSettings.withTestFixturesSourceSet).isTrue()
        assertThat(settings.sourceSetComponentSettings.testSourceSetName("domain")).isEqualTo("domainTest")
        assertThat(settings.sourceSetComponentSettings.testFixturesSourceSetName("domain")).isEqualTo("domainTestFixtures")
    }

    @Test
    fun `it should apply global source set configuration`() {
        val dsl = ArciphantDsl().apply {
            sourceSetComponentLayout()
            withTestSourceSet(false)
            withTestFixturesSourceSet(false)
            testSourceSetName { "test-$it" }
            testFixturesSourceSetName { "fixtures-$it" }
        }

        val settings = GlobalSettingsRepository(dsl).load()

        assertThat(settings.sourceSetComponentSettings.withTestSourceSet).isFalse()
        assertThat(settings.sourceSetComponentSettings.withTestFixturesSourceSet).isFalse()
        assertThat(settings.sourceSetComponentSettings.testSourceSetName("domain")).isEqualTo("test-domain")
        assertThat(settings.sourceSetComponentSettings.testFixturesSourceSetName("domain")).isEqualTo("fixtures-domain")
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `it should not derive test fixtures source set from the test source set setting`(value: Boolean) {
        val dsl = ArciphantDsl().apply {
            sourceSetComponentLayout()
            withTestSourceSet(value)
        }

        val settings = GlobalSettingsRepository(dsl).load()

        assertThat(settings.sourceSetComponentSettings.withTestSourceSet).isEqualTo(value)
        assertThat(settings.sourceSetComponentSettings.withTestFixturesSourceSet).isTrue()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `it should reject 'withTestSourceSet' for project layout`(value: Boolean) {
        val dsl = ArciphantDsl().apply { withTestSourceSet(value) }

        val exception = assertThrows<IllegalArgumentException> { GlobalSettingsRepository(dsl).load() }

        assertThat(exception.message).isEqualTo(
            "Arciphant configuration error: 'withTestSourceSet' cannot be configured for component layout 'PROJECT'"
        )
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `it should reject 'withTestFixturesSourceSet' for project layout`(value: Boolean) {
        val dsl = ArciphantDsl().apply { withTestFixturesSourceSet(value) }

        val exception = assertThrows<IllegalArgumentException> { GlobalSettingsRepository(dsl).load() }

        assertThat(exception.message).isEqualTo(
            "Arciphant configuration error: 'withTestFixturesSourceSet' cannot be configured for component layout 'PROJECT'"
        )
    }

    @Test
    fun `it should reject 'testSourceSetName' for project layout`() {
        val dsl = ArciphantDsl().apply { testSourceSetName { "foo" } }

        val exception = assertThrows<IllegalArgumentException> { GlobalSettingsRepository(dsl).load() }

        assertThat(exception.message).isEqualTo(
            "Arciphant configuration error: 'testSourceSetName' cannot be configured for component layout 'PROJECT'"
        )
    }

    @Test
    fun `it should reject 'testFixturesSourceSetName' for project layout`() {
        val dsl = ArciphantDsl().apply { testFixturesSourceSetName { "foo" } }

        val exception = assertThrows<IllegalArgumentException> { GlobalSettingsRepository(dsl).load() }

        assertThat(exception.message).isEqualTo(
            "Arciphant configuration error: 'testFixturesSourceSetName' cannot be configured for component layout 'PROJECT'"
        )
    }

    @Test
    fun `it should reject 'disableQualifiedArchiveBaseName' for source set layout`() {
        val dsl = ArciphantDsl().apply {
            sourceSetComponentLayout()
            disableQualifiedArchiveBaseName()
        }

        val exception = assertThrows<IllegalArgumentException> { GlobalSettingsRepository(dsl).load() }

        assertThat(exception.message).isEqualTo(
            "Arciphant configuration error: 'disableQualifiedArchiveBaseName' cannot be configured for component layout 'SOURCE_SET'"
        )
    }

    @Test
    fun `it should accept empty config for project component layouts`() {
        val dsl = ArciphantDsl().apply {
            projectSetComponentLayout()
        }
        assertDoesNotThrow {
            GlobalSettingsRepository(dsl).load()
        }
    }

    @Test
    fun `it should accept empty config for source set component layouts`() {
        val dsl = ArciphantDsl().apply {
            sourceSetComponentLayout()
        }
        assertDoesNotThrow {
            GlobalSettingsRepository(dsl).load()
        }
    }
}
