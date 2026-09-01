plugins {
    id("module")
}

dependencies {
    implementation(lib("spring-boot-starter"))
}

tasks.named("jar") { enabled = false }
tasks.named("bootJar") { enabled = true }
