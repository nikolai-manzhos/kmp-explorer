import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

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

    sourceSets {
        val intTest by creating {
            compileClasspath += sourceSets.main.get().output
            runtimeClasspath += sourceSets.main.get().output
        }
        val e2eTest by creating {
            compileClasspath += sourceSets.main.get().output
            runtimeClasspath += sourceSets.main.get().output
        }
    }

    testSourceSets(sourceSets.getByName("e2eTest"))
}

val e2eTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.testImplementation.get())
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.testImplementation.get())
}

val integrationTestTask = tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests."
    group = "verification"
    testClassesDirs = sourceSets.getByName("intTest").output.classesDirs
    classpath = sourceSets.getByName("intTest").runtimeClasspath
    mustRunAfter(tasks.test)
}
val functionalTest = tasks.register<Test>("functionalTest") {
    description = "Runs the e2e tests."
    group = "verification"
    testClassesDirs = sourceSets.getByName("e2eTest").output.classesDirs
    classpath = sourceSets.getByName("e2eTest").runtimeClasspath
    mustRunAfter(tasks.test)
    mustRunAfter(tasks["publishToMavenLocal"])
    dependsOn(tasks["publishToMavenLocal"])
}

val compilations = project.extensions
    .getByType(KotlinJvmProjectExtension::class.java)
        .target.compilations

compilations.getByName(sourceSets.getByName("intTest").name)
    .associateWith(compilations.getByName(SourceSet.MAIN_SOURCE_SET_NAME));

tasks.check {
    dependsOn(integrationTestTask)
    dependsOn(functionalTest)
}

dependencies {
    compileOnly(libs.plugins.kotlinMultiplatform.toDep())
    implementation(libs.graphviz)
    implementation(libs.graphviz.js.engine)
    testImplementation(libs.junit.jupiter)
    intTestImplementation(libs.plugins.kotlinMultiplatform.toDep())
}

tasks.withType<Test>().configureEach {
    // Using JUnitPlatform for running tests
    useJUnitPlatform()
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}
