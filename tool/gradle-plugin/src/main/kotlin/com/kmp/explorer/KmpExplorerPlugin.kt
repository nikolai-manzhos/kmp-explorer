package com.kmp.explorer

import com.kmp.explorer.external.SourceSetType
import com.kmp.explorer.internal.parser.KmpProjectParser
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.provider.DefaultProvider
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register

internal class KmpExplorerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.allprojects.forEach { currentProject ->
            currentProject.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                currentProject.tasks.create("exploreGraph")
                    .dependsOn(currentProject.createTask(SourceSetType.MAIN))
                    .dependsOn(currentProject.createTask(SourceSetType.TEST))
            }
        }
    }

    private fun Project.createTask(type: SourceSetType): TaskProvider<KmpExplorerTask> {
        val taskName = type.name.lowercase().capitalize()
        return tasks.register<KmpExplorerTask>("explore${taskName}Graph") {
            sourceSetType.set(type)
            kmpProjectStructureProperty.set(DefaultProvider {
                KmpProjectParser(type).parse(this@createTask)
            })
            val outputName = "kmp-${type.name.lowercase()}-hierarchy.png"
            hierarchyOutput.set(project.layout.buildDirectory.file(outputName))
        }
    }
}