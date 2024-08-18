plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm("jvm1")
    jvm("jvm2")
}