import ch.ergon.arciphant.dsl.ArciphantProjectDsl

tasks.named("bootJar") { enabled = true }
tasks.named("jar") { enabled = false }

configure<ArciphantProjectDsl> {
    absolutePackageName = "ch.ergon.arciphant.example"
}
