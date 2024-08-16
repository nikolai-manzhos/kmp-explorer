package com.kmp.explorer.internal.parser

import com.kmp.explorer.external.SourceSetType
import com.kmp.explorer.internal.KmpProjectStructure
import com.kmp.explorer.internal.KmpSourceNode
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.*
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget

private const val KMP_PLUGIN_ID = "org.jetbrains.kotlin.multiplatform"

internal class KmpProjectParser(
    private val type: SourceSetType
) {

    fun parse(project: Project): KmpProjectStructure {
        val projectDependencies = mutableMapOf<String, MutableSet<String>>()
        val graphByProject =
            mutableMapOf<String, MutableMap<KmpSourceNode, MutableList<KmpSourceNode>>>()
        buildKmpGraph(project, projectDependencies, graphByProject, null, null)
        return KmpProjectStructure(project.path, projectDependencies, graphByProject)
    }

    private fun buildKmpGraph(
        project: Project,
        projectStructure: MutableMap<String, MutableSet<String>>,
        graphByProject: MutableMap<String, MutableMap<KmpSourceNode, MutableList<KmpSourceNode>>>,
        parentSourceSets: Set<KotlinSourceSet>?,
        parentTargets: Set<String>?
    ) {
        val projectPath = project.path
        if (graphByProject.contains(projectPath)) return
        val graph = graphByProject.computeIfAbsent(projectPath) { mutableMapOf() }
        val kmp = project.extensions.getByType<KotlinMultiplatformExtension>()
        val kmpSourceNodes = mutableMapOf<String, KmpSourceNode>()
        val kmpSourceSetToTarget = mutableMapOf<String, String>()
        kmp.targets.forEach { target ->
            target.compilations.flatMap { it.kotlinSourceSets }
                .map(KotlinSourceSet::getName)
                .filterNot(type::shouldBeExcluded)
                .forEach { sourceSet ->
                    kmpSourceSetToTarget[sourceSet] = target.extractFullName()
                }
        }
        kmp.sourceSets.filterNot { type.shouldBeExcluded(it.name) }.forEach { ss ->
            val currentNode = kmpSourceNodes.getOrPut(ss.name) {
                KmpSourceNode(
                    ss.name,
                    findVisibility(ss.name, parentSourceSets)
                )
            }
            graph.computeIfAbsent(currentNode) { mutableListOf() }
            ss.dependsOn.filterNot { type.shouldBeExcluded(it.name) }.forEach { parent ->
                val parentNode = kmpSourceNodes.getOrPut(parent.name) {
                    KmpSourceNode(
                        parent.name,
                        findVisibility(parent.name, parentSourceSets)
                    )
                }
                graph.computeIfAbsent(parentNode) { mutableListOf() }
                    .add(currentNode)
            }
            // Remove redundant connection to common(Main/Test)
            // when the node is connected transitively
            val children = graph.values.flatten()
            children.forEach { node ->
                val count = children.count { it == node }
                if (count >= 2) {
                    graph.getValue(kmpSourceNodes.getValue(type.value))
                        .remove(node)
                }
            }

            graph.keys
                .filterNot { it.isVisible }
                .forEach { invisibleNode ->
                    val currentTarget = kmpSourceSetToTarget[invisibleNode.name]
                    val targetsCount = kmpSourceSetToTarget.count { (_, target) -> target == currentTarget }
                    val hasCertainTarget = parentTargets?.let {
                        parentTargets.contains(currentTarget) && targetsCount == 1
                    } ?: false
                    if (isConnectedToVisibleNode(invisibleNode, graph) || hasCertainTarget) {
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

                    val nextParentTargets = if (parentTargets == null)  {
                        kmpSourceSetToTarget.values.toSet()
                    } else emptySet()

                    buildKmpGraph(
                        projectDep,
                        projectStructure,
                        graphByProject,
                        parentSourceSets ?: kmp.sourceSets.toSet(),
                        nextParentTargets
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

    private fun KotlinTarget.extractFullName(): String {
        val postfix = when (platformType) {
            common -> ""
            jvm -> ""
            js -> ""
            androidJvm -> (this as KotlinAndroidTarget).name
            native -> (this as KotlinNativeTarget).konanTarget.name
            wasm -> (this as KotlinJsIrTarget).wasmTargetType?.name
        }
        return "${platformType.name}:$postfix"
    }
}