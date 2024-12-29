package ch.cbossi.gradle.modulith

import org.junit.jupiter.api.TestFactory

class GradleProjectPathTest {

    @TestFactory
    fun testValue() = dynamicTest(
        GradleProjectPath() to ":",
        GradleProjectPath("module") to ":module",
        GradleProjectPath("module", "component") to ":module:component",
        GradleProjectPath("any", "nested", "project", "structure") to ":any:nested:project:structure",
    ) { it.value }

    @TestFactory
    fun testIsRoot() = dynamicTest(
        GradleProjectPath() to true,
        GradleProjectPath("module") to false,
        GradleProjectPath("module", "component") to false,
    ) { it.isRoot }

}