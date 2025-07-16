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

  val basicLibraryStructure = stencil {
    defaultComponentPlugin("base-component")

    addComponent(api)
    addComponent(domain).withPlugin("domain-component").dependsOnApi(api)
    addComponent(webApi).withPlugin("web-component")
    addComponent(dbSchema)
    addComponent(db).withPlugin("db-component").dependsOn(dbSchema, domain)
  }

  val commonModuleStructure = stencil {
    basedOn(basicLibraryStructure)

    addComponent(web).withPlugin("web-component").dependsOn(webApi, domain)
  }

  library("shared") {
    basedOn(basicLibraryStructure)

    val base = addComponent("base")
    getComponent(api).dependsOnApi(base)
    getComponent(webApi).dependsOnApi(base)
  }
  module("customer") {
    basedOn(commonModuleStructure)
  }
  module("order") {
    basedOn(commonModuleStructure)

    addComponent("external-api").dependsOn(domain)
  }

  bundle("app") {
    withPlugin("bundle-module")
  }
}

// components the production code may depend on (repositories or other gradle builds)
dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}
