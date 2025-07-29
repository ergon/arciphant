pluginManagement {
  repositories {
    gradlePluginPortal()
  }
  includeBuild("./build-logic")
  includeBuild("../arciphant-gradle-plugin")
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}

plugins {
  id("ch.ergon.arciphant")
  id("spring-component") apply false // solely used to ensure plugin resolution mechanism for prebuilt plugins in 'build-logic' is triggered.
}

arciphant {
  val api = "api"
  val domain = "domain"
  val web = "web"
  val webApi = "web-api"
  val db = "db"
  val filestore = "filestore"

  val commonModuleStructure = stencil {
    addComponent(api).withPlugin("spring-component")
    addComponent(domain).withPlugin("spring-component").dependsOnApi(api)
    addComponent(db).withPlugin("jooq-component").dependsOn(domain)
    addComponent(webApi).withPlugin("spring-web-component")
    addComponent(web).withPlugin("spring-web-component").dependsOn(webApi, domain)
  }

  val commonModuleWithFilestoreStructure = stencil {
    basedOn(commonModuleStructure)
    addComponent(filestore).withPlugin("minio-component").dependsOn(domain)
  }

  library("shared") { basedOn(commonModuleWithFilestoreStructure) }

  module("course") { basedOn(commonModuleStructure) }
  module("exam") { basedOn(commonModuleStructure) }
  module("certificate") {
    basedOn(commonModuleWithFilestoreStructure)
    addComponent("certificate-authority-adapter").withPlugin("spring-component").dependsOn(domain)
  }
  module("accounting") {
    basedOn(commonModuleWithFilestoreStructure)
    val ppa = addComponent("payment-provider-adapter").withPlugin("spring-component").dependsOn(domain)
    getComponent(web).dependsOn(ppa)
  }

  bundle(name = "online-learning-platform", plugin = "spring-boot-bundle-module")
}
