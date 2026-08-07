plugins {
    id("ch.ergon.arciphant")
}

arciphant {
    absolutePackageName = "ch.ergon.arciphant.example"
}

tasks.named("bootJar") { enabled = true }
tasks.named("jar") { enabled = false }