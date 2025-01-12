plugins {
    id("idea")
}

idea {
    project {
        setLanguageLevel(JavaVersion.VERSION_21)
        targetBytecodeVersion = JavaVersion.VERSION_21
    }
}
