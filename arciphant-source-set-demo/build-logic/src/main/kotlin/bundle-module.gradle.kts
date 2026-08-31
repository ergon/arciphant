plugins {
    id("module")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
}

tasks.named("jar") { enabled = false }
tasks.named("bootJar") { enabled = true }
