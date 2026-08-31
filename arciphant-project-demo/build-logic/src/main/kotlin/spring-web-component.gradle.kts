plugins {
    id("spring-component")
}

dependencies {
    implementation(lib("spring-boot-starter-web"))
    testFixturesImplementation(lib("spring-boot-starter-web"))
}
