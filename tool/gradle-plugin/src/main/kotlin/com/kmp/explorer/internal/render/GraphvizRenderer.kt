package com.kmp.explorer.internal.render

import com.kmp.explorer.external.SourceSetType
import com.kmp.explorer.internal.KmpProjectStructure
import com.kmp.explorer.internal.KmpSourceNode
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.GraphAttr
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Rank
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.Factory
import guru.nidi.graphviz.model.Graph
import guru.nidi.graphviz.model.Link
import guru.nidi.graphviz.model.Node
import java.io.File

private class GraphvizRenderer(
    private val kmpProjectStructure: KmpProjectStructure,
    private val sourceSetType: SourceSetType,
    private val format: Format
) : Renderer {

    override fun render(output: File) {
        buildGraphvizOutput(output)
    }

    private fun buildGraphvizOutput(output: File) {
        val root = kmpProjectStructure.rootProjectPath
        val projectGraphs = kmpProjectStructure.projectsKmpGraph
        val projectsHierarchy = kmpProjectStructure.projectDependencies
        val clusters = mutableListOf<Graph>()
        val clusterLinks = mutableMapOf<String, String>()
        val queue = mutableListOf<String>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val links = projectGraphs.getValue(current)
                .flatMap { (root, children) ->
                    // We still want to include root node in a cluster,
                    // even if it's not connected
                    if (children.isEmpty()) {
                        listOf(createNode(current, root))
                    } else {
                        children.map { child ->
                            val from = createNode(current, root)
                            val to = createNode(current, child)
                            from.link(to)
                        }
                    }
                }
            clusters.add(
                Factory.graph(current)
                    .graphAttr()
                    .with(Label.of(current))
                    .cluster()
                    .named(current)
                    .with(links)
            )
            clusterLinks[current] = "$current:${findDeepestLeaf(projectGraphs.getValue(current))}"

            projectsHierarchy[current]?.forEach(queue::add)
        }

        val g = Factory.graph("root").directed()
            .graphAttr()
            .with(GraphAttr.COMPOUND)
            .graphAttr()
            .with(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT))
            .with(clusters)
            .with(projectsHierarchy.flatMap { (root, deps) ->
                deps.map { d ->
                    // Connect clusters with a link
                    // connection should always follow the following pattern:
                    // deepestNode -> commonMain/commonTest
                    createClusterConnection(root, d, clusterLinks)
                }
            })

        Graphviz.fromGraph(g).render(format).toFile(output)
    }

    /**
     * We have to find the deepest leaf in order to align clusters one after another.
     **/
    private fun findDeepestLeaf(graph: Map<KmpSourceNode, List<KmpSourceNode>>): String {
        var res = graph.keys.first { curr ->
            val allChildren = graph.values.flatten().toSet()
            !allChildren.contains(curr)
        }
        var maxDepth = 0
        val dfs = mutableListOf<Pair<KmpSourceNode, Int>>()
        dfs.add(Pair(res, 1))
        while (dfs.isNotEmpty()) {
            val curr = dfs.removeLast()
            val currNode = curr.first
            val currDepth = curr.second

            if (currDepth > maxDepth) {
                maxDepth = currDepth
                res = currNode
            }

            graph.getValue(currNode)
                .forEach { next -> dfs.add(Pair(next, currDepth + 1)) }
        }

        return res.name
    }

    private fun createNode(project: String, node: KmpSourceNode): Node {
        val color = if (node.isVisible) Color.RED else Color.BLACK
        return Factory
            .node("$project:${node.name}")
            .with(Label.of(node.name))
            .with(Shape.BOX)
            .with(color)
    }

    private fun createClusterConnection(
        from: String,
        to: String,
        clusterLinks: Map<String, String>
    ): Node {
        return Factory.node(clusterLinks.getValue(from))
            .link(
                Link.to(Factory.node("$to:${sourceSetType.value}"))
                    .with("ltail", "cluster_$from")
                    .with("lhead", "cluster_$to")
                    .with("minlen", 4)
            )
    }
}

internal fun createRenderer(
    kmpProjectStructure: KmpProjectStructure,
    type: SourceSetType,
    extension: Format,
): Renderer {
    return GraphvizRenderer(kmpProjectStructure, type, extension)
}

