import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

/**
 * Tests that use Gradle's [ProjectBuilder] fail by default when executed with the IntelliJ Runner:
 * org.gradle.api.GradleException: Could not inject synthetic classes.
 * java.lang.IllegalAccessException: module java.base does not open java.lang to unnamed module
 *
 * The cause is that [ProjectBuilder] is trying to access java.lang via reflection,
 * which is restricted by the Java Platform Module System (JPMS).
 *
 * To overcome this, the following VM option is required: --add-opens=java.base/java.lang=ALL-UNNAMED
 * This opens the java.lang package in the java.base module to unnamed modules (like the one created by ProjectBuilder).
 *
 * Gradle automatically sets this option, while IntelliJ does NOT. We manually have to set it when running a unit test.
 *
 * To set this on project-level for all unit tests we can modify the JUnit run configuration:
 * - Open to the Run Configuration Dropdown
 * - Click 'Edit configurations...' in the bottom
 * - Click 'Edit configuration templates...' in the bottom left
 * - Select the 'JUnit' template
 * - Add '--add-opens=java.base/java.lang=ALL-UNNAMED' to the vm options (the field with initial value '-ea')
 * - Choose the option 'Store as project file' in the top right, click 'OK' and add the stored file to VCS.
 *
 * additional comment:
 * In the gradle guide they talk about opening not only 'java.lang' but also 'java.util':
 * --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED
 * See: https://docs.gradle.org/current/userguide/upgrading_version_7.html
 * However, that does not seem to be necessary in our case. And other sources also only talk about 'java.lang', e.g.:
 * https://github.com/jvalentino/java-gradle-all-in-one-template?tab=readme-ov-file#unit-testing-plugins
 */
class ComponentPluginTest {

    @Test
    fun `plugins are applied`() {
        val project = ProjectBuilder.builder().build()

        project.pluginManager.apply("kotlin")
        project.pluginManager.apply("component")

        assertThat(project.plugins.hasPlugin("kotlin")).isTrue()
        assertThat(project.plugins.hasPlugin("java-test-fixtures")).isTrue()
    }

}
