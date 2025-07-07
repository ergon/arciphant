// plugins that extend the gradle build (gradle plugin portal, other binary repositories or local plugins from other builds)
pluginManagement {
  repositories {
    gradlePluginPortal()
  }
  includeBuild("../arciphant-gradle-plugin")
  includeBuild("./build-logic")
}

plugins {
  id("base-component") apply false // solely used to ensure plugin resolution mechanism for prebuilt plugins in 'build-logic' is triggered.
  id("ch.ergon.arciphant")
}

arciphant {
  val api = "api"
  val domain = "domain"
  val webApi = "web-api"
  val web = "web"
  val db = "db"
  val dbSchema = "db-schema"

  val libraryStencil = stencil {
    defaultComponentPlugin("base-component")

    addComponent(api)
    addComponent(domain).withPlugin("domain-component").dependsOnApi(api)
    addComponent(webApi).withPlugin("web-component")
    addComponent(dbSchema)
    addComponent(db).withPlugin("db-component").dependsOn(dbSchema, domain)
  }

  val moduleStencil = stencil {
    basedOn(libraryStencil)

    addComponent(web).withPlugin("web-component").dependsOn(webApi, domain)
  }

  library("shared") {
    basedOn(libraryStencil)

    val base = addComponent("base")
    getComponent(api).dependsOnApi(base)
    getComponent(webApi).dependsOnApi(base)
  }
  module("customer") {
    basedOn(moduleStencil)
  }
  module("order") {
    basedOn(moduleStencil)

    addComponent("external-api").dependsOn(domain)
  }

  bundle().withPlugin("bundle-module")
}

// components the production code may depend on (repositories or other gradle builds)
dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}
