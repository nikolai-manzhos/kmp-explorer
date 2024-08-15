plugins {
    `kotlin-dsl`
    `maven-publish`
}

gradlePlugin {
    plugins {
        register("kmpExplorer"){
            version = "0.2"
            id = "com.kmp.explorer"
            implementationClass = "com.kmp.explorer.KmpExplorerPlugin"
        }
    }
    publishing {
        repositories {
            mavenLocal()
        }
    }
    dependencies {
        compileOnly(libs.plugins.kotlinMultiplatform.toDep())
        implementation(libs.graphviz)
        implementation(libs.graphviz.js.engine)
        testImplementation(libs.junit.jupiter)
    }
}

sourceSets {
    val intTest by creating {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val intTestRuntimeOnly by configurations.getting

configurations["intTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())


tasks.withType<Test>().configureEach {
    // Using JUnitPlatform for running tests
    useJUnitPlatform()
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
