import ch.ergon.arciphant.core.ArciphantCorePlugin

// plugins that extend the gradle build (gradle plugin portal, other binary repositories or local plugins from other builds)
pluginManagement {
  repositories {
    gradlePluginPortal()
  }
  includeBuild("../arciphant-gradle-plugin")
  includeBuild("./build-logic")
}

plugins {
  id("component") apply false
  id("ch.ergon.arciphant.dsl")
}

arciphant {
  val api = "api"
  val domain = "domain"
  val webApi = "web-api"
  val web = "web"
  val db = "db"
  val dbSchema = "db-schema"
  val dbModel = "db-model"

  val libraryStencil = stencil {
    defaultComponentPlugin("component")

    addComponent(api)
    addComponent(domain).withPlugin("domain").dependsOnApi(api)
    addComponent(webApi).withPlugin("web")
    addComponent(dbSchema)
    addComponent(dbModel)
    addComponent(db).withPlugin("db").dependsOn(dbModel, domain)
  }

  val moduleStencil = stencil {
    basedOn(libraryStencil)

    addComponent(web).withPlugin("web").dependsOn(webApi, domain)
  }

  library("shared") {
    basedOn(libraryStencil)

    val base = addComponent("base")
    getComponent(api).dependsOnApi(base)
    getComponent(webApi).dependsOnApi(base)
  }
  module("customer") { basedOn(moduleStencil) }
  module("order") {
    basedOn(moduleStencil)

    addComponent("external-api").dependsOn(domain)
  }

  bundle().withPlugin("bundle")
}

// plugin should be applied after usage of the components-extension
apply<ArciphantCorePlugin>()

// components the production code may depend on (repositories or other gradle builds)
dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}
