package com.kmp.explorer.internal.parser

import com.kmp.explorer.external.SourceSetType
import com.kmp.explorer.internal.KmpProjectStructure
import com.kmp.explorer.internal.KmpSourceNode
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

private const val KMP_PLUGIN_ID = "org.jetbrains.kotlin.multiplatform"

internal class KmpProjectParser {

    fun parse(project: Project): KmpProjectStructure {
        val kmpProjectsRelation = mutableMapOf<String, MutableSet<String>>()
        val projectGraphs =
            mutableMapOf<String, MutableMap<KmpSourceNode, MutableList<KmpSourceNode>>>()
        buildKmpGraph(project, kmpProjectsRelation, projectGraphs, null)
        println(projectGraphs)
        return KmpProjectStructure(project.path, kmpProjectsRelation, projectGraphs)
    }

    private fun buildKmpGraph(
        project: Project,
        projectStructure: MutableMap<String, MutableSet<String>>,
        projectGraphs: MutableMap<String, MutableMap<KmpSourceNode, MutableList<KmpSourceNode>>>,
        parentSourceSets: Set<KotlinSourceSet>?
    ) {
        val projectPath = project.path
        if (projectGraphs.contains(projectPath)) return
        val graph = projectGraphs.computeIfAbsent(projectPath) { mutableMapOf() }
        val kmp = project.extensions.getByType<KotlinMultiplatformExtension>()
        val kmpSourceNodes = mutableMapOf<String, KmpSourceNode>()
        kmp.sourceSets.forEach { ss ->
            if (ss.name.lowercase().contains("test")) return@forEach
            val currentNode = kmpSourceNodes.getOrPut(ss.name) {
                KmpSourceNode(
                    ss.name,
                    findVisibility(ss.name, parentSourceSets)
                )
            }
            graph.computeIfAbsent(currentNode) { mutableListOf() }
            ss.dependsOn.forEach { parent ->
                val parentNode = kmpSourceNodes.getOrPut(parent.name) {
                    KmpSourceNode(
                        parent.name,
                        findVisibility(parent.name, parentSourceSets)
                    )
                }
                graph.computeIfAbsent(parentNode) { mutableListOf() }
                    .add(currentNode)
            }
            val children = graph.values.flatten()
            children.forEach { node ->
                val count = children.count { it == node }
                if (count >= 2) {
                    graph.getValue(kmpSourceNodes.getValue(SourceSetType.MAIN.value))
                        .remove(node)
                }
            }

            // Update visibility of transitive nodes.
            graph.keys
                .filterNot { it.isVisible }
                .forEach { invisibleNode ->
                    if (isConnectedToVisibleNode(invisibleNode, graph)) {
                        invisibleNode.isVisible = true
                    }
                }

            project.configurations
                .filter { c -> c.name == ss.implementationConfigurationName }
                .flatMap { c -> c.allDependencies.withType<ProjectDependency>() }
                .filter { pd -> pd.dependencyProject.pluginManager.hasPlugin(KMP_PLUGIN_ID) }
                .map(ProjectDependency::getDependencyProject)
                .forEach { projectDep ->
                    projectStructure.computeIfAbsent(project.path) { mutableSetOf() }
                        .add(projectDep.path)

                    buildKmpGraph(
                        projectDep,
                        projectStructure,
                        projectGraphs,
                        parentSourceSets ?: kmp.sourceSets.toSet()
                    )
                }
        }
    }

    private fun isConnectedToVisibleNode(
        node: KmpSourceNode,
        graph: MutableMap<KmpSourceNode, MutableList<KmpSourceNode>>
    ): Boolean {
        val connections = graph.getValue(node)
        if (connections.isEmpty()) return false

        connections.forEach { connection ->
            if (connection.isVisible) {
                return true
            } else {
                isConnectedToVisibleNode(connection, graph)
            }
        }

        return false
    }

    private fun findVisibility(name: String, parentSourceSets: Set<KotlinSourceSet>?): Boolean {
        return parentSourceSets?.map(KotlinSourceSet::getName)?.contains(name) ?: true
    }
}