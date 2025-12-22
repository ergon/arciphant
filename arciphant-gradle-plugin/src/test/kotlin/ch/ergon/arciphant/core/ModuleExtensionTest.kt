package ch.ergon.arciphant.core

import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.ComponentReference
import ch.ergon.arciphant.core.model.DomainModule
import ch.ergon.arciphant.core.model.ModuleReference
import ch.ergon.arciphant.util.dynamicTest
import org.junit.jupiter.api.TestFactory

class ModuleExtensionTest {

    @TestFactory
    fun testCreateQualifiedComponentName() = dynamicTest(
        TestCase("foo", "bar") to "foo-bar",
        TestCase("foo", "") to "foo",
        TestCase("", "bar") to "bar",
    ) {
        val module = DomainModule(ModuleReference(name = it.moduleName), emptySet())
        val component = Component(ComponentReference(it.componentName), null, emptySet())
        module.createQualifiedComponentName(component)
    }

    private data class TestCase(val moduleName: String, val componentName: String)

}
