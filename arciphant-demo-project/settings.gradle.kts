pluginManagement {
  repositories {
    gradlePluginPortal()
  }
  includeBuild("./build-logic")
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}

plugins {
  id("ch.ergon.arciphant") version "0.1.1"
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
    defaultComponentPlugin("spring-component")

    addComponent(api)
    addComponent(domain).dependsOnApi(api)
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
    addComponent("certificate-authority-adapter").dependsOn(domain)
  }
  module("accounting") {
    basedOn(commonModuleWithFilestoreStructure)
    val ppa = addComponent("payment-provider-adapter").dependsOn(domain)
    getComponent(web).dependsOn(ppa)
  }

  bundle("online-learning-platform") {
    withPlugin("spring-boot-bundle-module")
  }
}
