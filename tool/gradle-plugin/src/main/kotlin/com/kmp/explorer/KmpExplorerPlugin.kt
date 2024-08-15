package com.kmp.explorer

import com.kmp.explorer.internal.parser.KmpProjectParser
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.provider.DefaultProvider
import org.gradle.kotlin.dsl.register

internal class KmpExplorerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.allprojects.forEach { currentProject ->
            currentProject.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                currentProject.tasks.register<KmpExplorerTask>("exploreGraph") {
                    kmpProjectStructureProperty.set(DefaultProvider {
                        KmpProjectParser().parse(currentProject)
                    })
                    hierarchyOutput.set(project.layout.buildDirectory.file("kmp-hierarchy.png"))
                }
            }
        }
    }
}