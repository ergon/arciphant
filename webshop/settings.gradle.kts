import ch.cbossi.gradle.playground.build.ModulithSettingsPlugin

// plugins that extend the gradle build (gradle plugin portal, other binary repositories or local plugins from other builds)
pluginManagement {
  repositories {
    gradlePluginPortal()
  }
  includeBuild("../build-logic")
}

plugins {
  id("ch.cbossi.gradle.playground.build.modulith-configuration-settings-plugin")
}

modulith {
  val api = createComponent("api")
  val domain = createComponent("domain")
  val webApi = createComponent("web-api")
  val web = createComponent("web")
  val db = createComponent("db")
  val dbSchema = createComponent("db-schema")
  val dbModel = createComponent("db-model")


  allModules {
    addComponent(api)
    addComponent(domain).withPlugin("domain").dependsOnApi(api)
    addComponent(webApi)
    addComponent(web).dependsOn(webApi, domain)
    addComponent(dbSchema)
    addComponent(dbModel)
    addComponent(db).dependsOn(dbModel, domain)
  }

  module("shared") {
    removeComponent(webApi, web)
  }
  module("customer")
  module("order") {
    addComponent("api-client").dependsOn(domain)
  }
}

// plugin should be applied after usage of the components-extension
apply<ModulithSettingsPlugin>()

// components the production code may depend on (repositories or other gradle builds)
dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}
