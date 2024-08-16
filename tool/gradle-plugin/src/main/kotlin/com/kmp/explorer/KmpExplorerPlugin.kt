package com.kmp.explorer

import com.kmp.explorer.external.KmpExplorerExtension
import com.kmp.explorer.external.SourceSetType
import com.kmp.explorer.internal.parser.KmpProjectParser
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.provider.DefaultProvider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

internal class KmpExplorerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val ext = target.extensions.create<KmpExplorerExtension>("kmpExplorer")

        target.allprojects.forEach { currentProject ->
            currentProject.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                currentProject.tasks.create("exploreGraph")
                    .dependsOn(currentProject.createTask(SourceSetType.MAIN, ext))
                    .dependsOn(currentProject.createTask(SourceSetType.TEST, ext))
            }
        }
    }

    private fun Project.createTask(
        type: SourceSetType,
        extension: KmpExplorerExtension
    ): TaskProvider<KmpExplorerTask> {
        val taskName = type.name.lowercase().capitalize()
        return tasks.register<KmpExplorerTask>("explore${taskName}Graph") {
            formatProperty.set(extension.format)
            sourceSetTypeProperty.set(type)
            projectStructureProperty.set(DefaultProvider {
                KmpProjectParser(type).parse(this@createTask)
            })
            val fileExt = extension.format.fileExtension
            val outputName = "kmp-${type.name.lowercase()}-hierarchy.${fileExt}"
            hierarchyOutput.set(project.layout.buildDirectory.file(outputName))
        }
    }
}