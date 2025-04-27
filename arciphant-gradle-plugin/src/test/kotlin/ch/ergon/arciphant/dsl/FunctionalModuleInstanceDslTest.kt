package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.ComponentReference
import ch.ergon.arciphant.model.DependencyType
import ch.ergon.arciphant.model.Plugin
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FunctionalModuleInstanceDslTest {

    @Nested
    inner class ComponentsTest {

        private val component1a = ComponentReference("component1a")
        private val component1b = ComponentReference("component1b")
        private val component2a = ComponentReference("component2a")
        private val component2b = ComponentReference("component2b")
        private val component3a = ComponentReference("component3a")
        private val component3b = ComponentReference("component3b")

        @Test
        fun `it should merge components with base stencils`() {
            val stencil1 = stencil(components = listOf(component1a, component1b))
            val stencil2 = stencil(components = listOf(component2a, component2b))
            val dsl = FunctionalModuleStencilDsl().apply {
                basedOn(stencil1)
                basedOn(stencil2)
                addComponent(component3a.name)
                addComponent(component3b.name)
            }

            val stencil = dsl.build()

            assertThat(stencil).isEqualTo(
                stencil(
                    components = listOf(component1a, component1b, component2a, component2b, component3a, component3b)
                )
            )
        }

        @Test
        fun `it should merge components of base stencils`() {
            val stencil1 = stencil(components = listOf(component1a, component1b))
            val stencil2 = stencil(components = listOf(component2a, component2b))
            val dsl = FunctionalModuleStencilDsl().apply {
                basedOn(stencil1)
                basedOn(stencil2)
            }

            val stencil = dsl.build()

            assertThat(stencil).isEqualTo(
                stencil(
                    components = listOf(component1a, component1b, component2a, component2b)
                )
            )
        }

        @Test
        fun `it should take components from stencil`() {
            val dsl = FunctionalModuleStencilDsl().apply {
                addComponent(component3a.name)
                addComponent(component3b.name)
            }

            val stencil = dsl.build()

            assertThat(stencil).isEqualTo(
                stencil(
                    components = listOf(component3a, component3b)
                )
            )
        }

    }

    @Nested
    inner class DependenciesTest {

        private val component3a = ComponentReference("component3a")
        private val component3b = ComponentReference("component3b")
        private val dependency1a = componentDependency("source1a", "dependsOn1a")
        private val dependency1b = componentDependency("source1b", "dependsOn1b")
        private val dependency2a = componentDependency("source2a", "dependsOn2a")
        private val dependency2b = componentDependency("source2b", "dependsOn2b")
        private val dependency3a = componentDependency(component3a.name, "dependsOn3a")
        private val dependency3b = componentDependency(component3b.name, "dependsOn3b")

        @Test
        fun `it should merge dependencies with base stencils`() {
            val stencil1 = stencil(dependencies = listOf(dependency1a, dependency1b))
            val stencil2 = stencil(dependencies = listOf(dependency2a, dependency2b))
            val dsl = FunctionalModuleStencilDsl().apply {
                basedOn(stencil1)
                basedOn(stencil2)
                addComponent(component3a.name).dependsOn(dependency3a.dependsOn.name)
                addComponent(component3b.name).dependsOn(dependency3b.dependsOn.name)
            }

            val stencil = dsl.build()

            assertThat(stencil).isEqualTo(
                stencil(
                    components = listOf(component3a, component3b),
                    dependencies = listOf(
                        dependency1a,
                        dependency1b,
                        dependency2a,
                        dependency2b,
                        dependency3a,
                        dependency3b
                    ),
                )
            )
        }

        @Test
        fun `it should merge dependencies of base stencils`() {
            val stencil1 = stencil(dependencies = listOf(dependency1a, dependency1b))
            val stencil2 = stencil(dependencies = listOf(dependency2a, dependency2b))
            val dsl = FunctionalModuleStencilDsl().apply {
                basedOn(stencil1)
                basedOn(stencil2)
            }

            val stencil = dsl.build()

            assertThat(stencil).isEqualTo(
                stencil(
                    dependencies = listOf(dependency1a, dependency1b, dependency2a, dependency2b),
                )
            )
        }

        @Test
        fun `it should take dependencies from stencil`() {
            val dsl = FunctionalModuleStencilDsl().apply {
                addComponent(component3a.name).dependsOn(dependency3a.dependsOn.name)
                addComponent(component3b.name).dependsOn(dependency3b.dependsOn.name)
            }

            val stencil = dsl.build()

            assertThat(stencil).isEqualTo(
                stencil(
                    components = listOf(component3a, component3b),
                    dependencies = listOf(dependency3a, dependency3b),
                )
            )
        }

    }

    @Nested
    inner class ComponentPluginsTest {
        private val component1a = ComponentReference("component1a")
        private val component1b = ComponentReference("component1b")
        private val component2a = ComponentReference("component2a")
        private val component2b = ComponentReference("component2b")
        private val component3a = ComponentReference("component3a")
        private val component3b = ComponentReference("component3b")
        private val plugin1a = Plugin("plugin1a")
        private val plugin1b = Plugin("plugin1b")
        private val plugin2a = Plugin("plugin2a")
        private val plugin2b = Plugin("plugin2b")
        private val plugin3a = Plugin("plugin3a")
        private val plugin3b = Plugin("plugin3b")

        @Test
        fun `it should merge component plugins with base stencils`() {
            val stencil1 = stencil(componentPlugins = mapOf(component1a to plugin1a, component1b to plugin1b))
            val stencil2 = stencil(componentPlugins = mapOf(component2a to plugin2a, component2b to plugin2b))
            val dsl = FunctionalModuleStencilDsl().apply {
                basedOn(stencil1)
                basedOn(stencil2)
                addComponent(component3a.name).withPlugin(plugin3a.id)
                addComponent(component3b.name).withPlugin(plugin3b.id)
            }

            val stencil = dsl.build()

            assertThat(stencil).isEqualTo(
                stencil(
                    components = listOf(component3a, component3b),
                    componentPlugins = mapOf(
                        component1a to plugin1a,
                        component1b to plugin1b,
                        component2a to plugin2a,
                        component2b to plugin2b,
                        component3a to plugin3a,
                        component3b to plugin3b
                    )
                )
            )
        }

        @Test
        fun `it should merge component plugins of base stencils`() {
            val stencil1 = stencil(componentPlugins = mapOf(component1a to plugin1a, component1b to plugin1b))
            val stencil2 = stencil(componentPlugins = mapOf(component2a to plugin2a, component2b to plugin2b))
            val dsl = FunctionalModuleStencilDsl().apply {
                basedOn(stencil1)
                basedOn(stencil2)
            }

            val stencil = dsl.build()

            assertThat(stencil).isEqualTo(
                stencil(
                    componentPlugins = mapOf(
                        component1a to plugin1a,
                        component1b to plugin1b,
                        component2a to plugin2a,
                        component2b to plugin2b,
                    )
                )
            )
        }

        @Test
        fun `it should take component plugins from stencil`() {
            val dsl = FunctionalModuleStencilDsl().apply {
                addComponent(component3a.name).withPlugin(plugin3a.id)
                addComponent(component3b.name).withPlugin(plugin3b.id)
            }

            val stencil = dsl.build()

            assertThat(stencil).isEqualTo(
                stencil(
                    components = listOf(component3a, component3b),
                    componentPlugins = mapOf(
                        component3a to plugin3a,
                        component3b to plugin3b
                    )
                )
            )
        }
    }

    @Nested
    inner class DefaultComponentPluginTest {
        private val plugin1 = Plugin("plugin1")
        private val plugin2 = Plugin("plugin2")
        private val plugin3 = Plugin("plugin3")

        @Test
        fun `it should override plugins of base stencils`() {
            val stencil1 = stencil(defaultComponentPlugin = plugin1)
            val stencil2 = stencil(defaultComponentPlugin = plugin2)
            val dsl = FunctionalModuleStencilDsl().apply {
                basedOn(stencil1)
                basedOn(stencil2)
                defaultComponentPlugin(plugin3.id)
            }

            val stencil = dsl.build()

            assertThat(stencil).isEqualTo(stencil(defaultComponentPlugin = plugin3))
        }

        @Test
        fun `it should fallback to plugin of latest base stencil`() {
            val stencil1 = stencil(defaultComponentPlugin = plugin1)
            val stencil2 = stencil(defaultComponentPlugin = plugin2)
            val dsl = FunctionalModuleStencilDsl().apply {
                basedOn(stencil1)
                basedOn(stencil2)
            }

            val stencil = dsl.build()

            assertThat(stencil).isEqualTo(stencil(defaultComponentPlugin = plugin2))
        }
    }

    private fun stencil(
        components: List<ComponentReference> = emptyList(),
        dependencies: List<ComponentDependency> = emptyList(),
        componentPlugins: Map<ComponentReference, Plugin> = emptyMap(),
        defaultComponentPlugin: Plugin? = null,
    ) = Stencil(components, dependencies, componentPlugins, defaultComponentPlugin)

    private fun componentDependency(source: String, dependsOn: String) =
        ComponentDependency(ComponentReference(source), DependencyType.IMPLEMENTATION, ComponentReference(dependsOn))

}
