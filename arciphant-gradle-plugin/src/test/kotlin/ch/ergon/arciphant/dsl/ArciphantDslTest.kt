package ch.ergon.arciphant.dsl

import ch.ergon.arciphant.model.*
import ch.ergon.arciphant.model.DependencyType.API
import ch.ergon.arciphant.model.DependencyType.IMPLEMENTATION
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ArciphantDslTest {

    private val dsl = ArciphantDsl()
    private val repository = DslModuleRepository(dsl)

    private val sharedModuleRef = LibraryModuleReference("shared")
    private val customerModuleRef = DomainModuleReference("customer")
    private val orderModuleRef = DomainModuleReference("order")
    private val inventoryModuleRef = DomainModuleReference("inventory")
    private val orderingModuleRef = BundleModuleReference("ordering")
    private val appModuleRef = BundleModuleReference("app")

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
            val common = stencil {
                val domain = addComponent(domainComponentRef).withPlugin(domainPlugin.id)
                addComponent(dbComponentRef).withPlugin(dbPlugin.id).dependsOn(domain)
            }
            val web = stencil {
                val webApi = addComponent(webApiComponentRef)
                addComponent(webComponentRef).dependsOnApi(webApi)
            }
            library("shared") {
                basedOn(common)
                val base = addComponent(baseComponentRef)
                getComponent(domainComponentRef.name).dependsOn(base)
                getComponent(dbComponentRef.name).dependsOn(base)
            }
            val customer = module("customer") {
                basedOn(common)
                basedOn(web)
            }
            val order = module("order") {
                basedOn(common)
                basedOn(web)
                addComponent(externalApiComponentRef)
            }
            module("inventory") {
                addComponent("main")
            }
            bundle("ordering", includes = setOf(customer, order))
            bundle("app", plugin = bundlePlugin.id)
        }

        val modules = repository.load()

        val domainComponent = Component(
            reference = domainComponentRef,
            plugin = domainPlugin,
            dependsOn = emptyList(),
        )
        val dbComponent = Component(
            reference = dbComponentRef,
            plugin = dbPlugin,
            dependsOn = listOf(Dependency(component = domainComponentRef, type = IMPLEMENTATION)),
        )
        val webApiComponent = Component(
            reference = webApiComponentRef,
            plugin = null,
            dependsOn = emptyList(),
        )
        val webComponent = Component(
            reference = webComponentRef,
            plugin = null,
            dependsOn = listOf(Dependency(component = webApiComponentRef, type = API)),
        )
        val externalApiComponent = Component(
            reference = externalApiComponentRef,
            plugin = null,
            dependsOn = emptyList(),
        )

        assertThat(modules).containsExactlyInAnyOrder(
            LibraryModule(
                reference = sharedModuleRef,
                components = setOf(
                    domainComponent.copy(
                        dependsOn = listOf(Dependency(component = baseComponentRef, type = IMPLEMENTATION))
                    ),
                    dbComponent.copy(
                        dependsOn =
                            listOf(
                                Dependency(component = domainComponentRef, type = IMPLEMENTATION),
                                Dependency(component = baseComponentRef, type = IMPLEMENTATION),
                            )
                    ),
                    Component(
                        reference = baseComponentRef,
                        plugin = null,
                        dependsOn = emptyList(),
                    ),
                ),
            ),
            DomainModule(
                reference = customerModuleRef,
                components = setOf(domainComponent, dbComponent, webApiComponent, webComponent),
            ),
            DomainModule(
                reference = orderModuleRef,
                components = setOf(domainComponent, dbComponent, webApiComponent, webComponent, externalApiComponent),
            ),
            DomainModule(
                reference = inventoryModuleRef,
                components = setOf(
                    Component(
                        reference = ComponentReference("main"),
                        plugin = null,
                        dependsOn = emptyList(),
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
