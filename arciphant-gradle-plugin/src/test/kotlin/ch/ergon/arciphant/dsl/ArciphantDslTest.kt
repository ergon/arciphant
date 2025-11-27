package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.core.ModuleRepository
import ch.ergon.arciphant.core.model.*
import ch.ergon.arciphant.core.model.BundleModule
import ch.ergon.arciphant.core.model.Component
import ch.ergon.arciphant.core.model.Dependency
import ch.ergon.arciphant.core.model.DependencyType.API
import ch.ergon.arciphant.core.model.DependencyType.IMPLEMENTATION
import ch.ergon.arciphant.core.model.DomainModule
import ch.ergon.arciphant.core.model.LibraryModule
import ch.ergon.arciphant.core.model.Plugin
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ArciphantDslTest {

    private val dsl = ArciphantDsl()
    private val moduleRepository = ModuleRepository(dsl)

    @Nested
    inner class ComponentsTest {

        private val component = "my-component"
        private val duplicateComponentNameMessage =
            "Arciphant configuration error: Component with name '$component' has already been declared. Use 'extendComponent' instead of 'createComponent' to extend an existing component."

        private val component1a = ComponentReference("component1a")
        private val component1b = ComponentReference("component1b")
        private val component2a = ComponentReference("component2a")
        private val component2b = ComponentReference("component2b")
        private val component3a = ComponentReference("component3a")
        private val component3b = ComponentReference("component3b")

        @Test
        fun `it should merge components of templates and module itself`() {
            with(dsl) {
                val template1 = template()
                    .createComponent(component1a.name)
                    .createComponent(component1b.name)
                val template2 = template()
                    .createComponent(component2a.name)
                    .createComponent(component2b.name)
                module(name = "module", templates = setOf(template1, template2))
                    .createComponent(component3a.name)
                    .createComponent(component3b.name)
            }

            val module = moduleRepository.loadSingleModule()

            assertThat(module.components.map { it.reference }).containsExactlyInAnyOrder(
                component1a, component1b, component2a, component2b, component3a, component3b
            )
        }

        @Test
        fun `it should support multiple extends`() {
            with(dsl) {
                val template1 = template()
                    .createComponent(component1a.name)
                    .createComponent(component1b.name)
                val template2 = template()
                    .createComponent(component2a.name)
                    .createComponent(component2b.name)
                val template = template()
                    .extends(template1)
                    .extends(template2)
                    .createComponent(component3a.name)
                    .createComponent(component3b.name)
                module(name = "module", templates = setOf(template))
            }

            val module = moduleRepository.loadSingleModule()

            assertThat(module.components.map { it.reference }).containsExactlyInAnyOrder(
                component1a, component1b, component2a, component2b, component3a, component3b
            )
        }

        @Test
        fun `it should not allow duplicate component names in same module`() {
            with(dsl) {
                module(name = "module")
                    .createComponent(component)
                    .createComponent(component)
            }

            val exception = assertThrows<IllegalArgumentException> {
                moduleRepository.load()
            }

            assertThat(exception.message).isEqualTo(duplicateComponentNameMessage)
        }

        @Test
        fun `it should not allow component name that is already present in template`() {
            with(dsl) {
                val baseTemplate = template()
                    .createComponent(component)
                module(name = "module", template = baseTemplate)
                    .createComponent(component)
            }

            val exception = assertThrows<IllegalArgumentException> {
                moduleRepository.load()
            }

            assertThat(exception.message).isEqualTo(duplicateComponentNameMessage)
        }

        @Test
        fun `it should not allow component name that is already present in extended template`() {
            with(dsl) {
                val baseTemplate = template()
                    .createComponent(component)
                val template = template()
                    .extends(baseTemplate)
                    .createComponent(component)
                module(name = "module", templates = setOf(template))
            }

            val exception = assertThrows<IllegalArgumentException> {
                moduleRepository.load()
            }

            assertThat(exception.message).isEqualTo(duplicateComponentNameMessage)
        }

        @Test
        fun `it should not allow multiple component extension in same module`() {
            with(dsl) {

                val template = template()
                    .createComponent(component)

                val exception = assertThrows<IllegalArgumentException> {
                    module(name = "module", template = template)
                        .extendComponent(component)
                        .extendComponent(component)
                }

                assertThat(exception.message).isEqualTo("Arciphant configuration error: Component '$component' has already been extended in the current context.")
            }
        }

        @Test
        fun `it should not allow extending a component that does not exist`() {
            with(dsl) {
                module(name = "module")
                    .extendComponent(component)
            }

            val exception = assertThrows<IllegalArgumentException> {
                moduleRepository.load()
            }

            assertThat(exception.message).isEqualTo("Arciphant configuration error: Component with name '$component' does not exist. Use 'createComponent' instead of 'extendComponent' to create a new component.")
        }
    }

    @Nested
    inner class NameVerificationTest {
        @Test
        fun `it should prevent component name with whitespaces`() {
            with(dsl) {

                val exception = assertThrows<IllegalArgumentException> {
                    template()
                        .createComponent("my component")
                }

                assertThat(exception.message).isEqualTo("Arciphant configuration error: component name must not contain whitespaces")
            }
        }

        @Test
        fun `it should prevent empty component name`() {
            with(dsl) {

                assertDoesNotThrow {
                    template()
                        .createComponent("")
                }
            }
        }

        @Test
        fun `it should prevent library name with whitespaces`() {
            with(dsl) {

                val exception = assertThrows<IllegalArgumentException> {
                    library("my library")
                }

                assertThat(exception.message).isEqualTo("Arciphant configuration error: library name must not contain whitespaces")
            }
        }

        @Test
        fun `it should accept empty library name`() {
            with(dsl) {

                assertDoesNotThrow {
                    library("")
                }
            }
        }

        @Test
        fun `it should prevent module name with whitespaces`() {
            with(dsl) {

                val exception = assertThrows<IllegalArgumentException> {
                    module("my module")
                }

                assertThat(exception.message).isEqualTo("Arciphant configuration error: module name must not contain whitespaces")
            }
        }

        @Test
        fun `it should accept empty module name`() {
            with(dsl) {

                assertDoesNotThrow {
                    module("")
                }
            }
        }
    }

    @Nested
    inner class DependenciesTest {
        private val sourceComponent = "sourceComponent"
        private val targetComponent1a = "targetComponent1a"
        private val targetComponent1b = "targetComponent1b"
        private val targetComponent2 = "targetComponent2"
        private val targetComponent3a = "targetComponent3a"
        private val targetComponent3b = "targetComponent3b"

        @Test
        fun `it should apply dependency type`() {
            with(dsl) {
                module(name = "module")
                    .createComponent(
                        name = sourceComponent,
                        dependsOnApi = setOf(targetComponent3a),
                        dependsOn = setOf(targetComponent3b)
                    )
            }

            val component = moduleRepository.loadSingleComponent()

            assertThat(component.dependsOn).containsExactlyInAnyOrder(
                Dependency(component = ComponentReference(targetComponent3a), type = API),
                Dependency(component = ComponentReference(targetComponent3b), type = IMPLEMENTATION),
            )

        }

        @Test
        fun `it should merge dependencies`() {
            with(dsl) {
                val template1 = template()
                    .createComponent(name = sourceComponent, dependsOn = setOf(targetComponent1a, targetComponent1b))
                val template2 = template()
                    .extends(template1)
                    .extendComponent(name = sourceComponent, dependsOn = setOf(targetComponent2))
                module(name = "module", templates = setOf(template2))
                    .extendComponent(name = sourceComponent, dependsOn = setOf(targetComponent3a, targetComponent3b))
            }

            val component = moduleRepository.loadSingleComponent()

            assertThat(component.dependsOn.map { it.component.name }).containsExactlyInAnyOrder(
                targetComponent1a, targetComponent1b, targetComponent2, targetComponent3a, targetComponent3b,
            )
        }
    }

    @Nested
    inner class CompleteExampleTest {
        private val path = emptyList<String>()

        private val sharedModuleRef = ModuleReference(name =  "shared")
        private val customerModuleRef = ModuleReference(name = "customer")
        private val orderModuleRef = ModuleReference(name = "order")
        private val inventoryModuleRef = ModuleReference(name = "inventory")
        private val orderingModuleRef = ModuleReference(name = "ordering")
        private val appModuleRef = ModuleReference(name = "app")

        private val baseComponentRef = ComponentReference("base")
        private val domainComponentRef = ComponentReference("domain")
        private val dbComponentRef = ComponentReference("db")
        private val webApiComponentRef = ComponentReference("web-api")
        private val webComponentRef = ComponentReference("web")
        private val externalApiComponentRef = ComponentReference("external-api")

        private val domainPlugin = Plugin("domain-plugin")
        private val dbPlugin = Plugin("db-plugin")
        private val bundlePlugin = Plugin("bundle-plugin")

        @Test
        fun testDsl() {
            with(dsl) {
                val common = template()
                    .createComponent(name = "domain", plugin = domainPlugin.id)
                    .createComponent(name = "db", plugin = dbPlugin.id, dependsOn = setOf("domain"))
                val web = template()
                    .createComponent(name = "web-api")
                    .createComponent(name = "web", dependsOnApi = setOf("web-api"))
                library(name = "shared", template = common)
                    .createComponent("base")
                    .extendComponent("domain", dependsOn = setOf("base"))
                    .extendComponent("db", dependsOn = setOf("base"))

                val customer = module(name = "customer", templates = setOf(common, web))
                val order = module(name = "order", templates = setOf(common, web))
                    .createComponent(name = "external-api")
                module(name = "inventory")
                    .createComponent(name = "main")
                bundle("ordering", includes = setOf(customer, order))
                bundle("app", plugin = bundlePlugin.id)
            }

            val modules = moduleRepository.load()

            val domainComponent = Component(
                reference = domainComponentRef,
                plugin = domainPlugin,
                dependsOn = emptySet(),
            )
            val dbComponent = Component(
                reference = dbComponentRef,
                plugin = dbPlugin,
                dependsOn = setOf(Dependency(component = domainComponentRef, type = IMPLEMENTATION)),
            )
            val webApiComponent = Component(
                reference = webApiComponentRef,
                plugin = null,
                dependsOn = emptySet(),
            )
            val webComponent = Component(
                reference = webComponentRef,
                plugin = null,
                dependsOn = setOf(Dependency(component = webApiComponentRef, type = API)),
            )
            val externalApiComponent = Component(
                reference = externalApiComponentRef,
                plugin = null,
                dependsOn = emptySet(),
            )

            assertThat(modules).containsExactlyInAnyOrder(
                LibraryModule(
                    reference = sharedModuleRef,
                    components = setOf(
                        domainComponent.copy(
                            dependsOn = setOf(Dependency(component = baseComponentRef, type = IMPLEMENTATION))
                        ),
                        dbComponent.copy(
                            dependsOn =
                                setOf(
                                    Dependency(component = domainComponentRef, type = IMPLEMENTATION),
                                    Dependency(component = baseComponentRef, type = IMPLEMENTATION),
                                )
                        ),
                        Component(
                            reference = baseComponentRef,
                            plugin = null,
                            dependsOn = emptySet(),
                        ),
                    ),
                ),
                DomainModule(
                    reference = customerModuleRef,
                    components = setOf(domainComponent, dbComponent, webApiComponent, webComponent),
                ),
                DomainModule(
                    reference = orderModuleRef,
                    components = setOf(
                        domainComponent,
                        dbComponent,
                        webApiComponent,
                        webComponent,
                        externalApiComponent
                    ),
                ),
                DomainModule(
                    reference = inventoryModuleRef,
                    components = setOf(
                        Component(
                            reference = ComponentReference("main"),
                            plugin = null,
                            dependsOn = emptySet(),
                        )
                    ),
                ),
                BundleModule(
                    reference = orderingModuleRef,
                    plugin = null,
                    includes = setOf(customerModuleRef, orderModuleRef)
                ),
                BundleModule(
                    reference = appModuleRef,
                    plugin = bundlePlugin,
                    includes = setOf(sharedModuleRef, customerModuleRef, orderModuleRef, inventoryModuleRef),
                ),
            )
        }

    }

}

private fun ModuleRepository.loadSingleComponent() = loadSingleModule().components.single()

private fun ModuleRepository.loadSingleModule() = load().single() as DomainModule
