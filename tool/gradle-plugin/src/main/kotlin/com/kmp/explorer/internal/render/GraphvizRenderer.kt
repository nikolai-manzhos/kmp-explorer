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
) : Renderer {

    override fun render(output: File) {
        buildGraphvizOutput(
            kmpProjectStructure.rootProjectPath,
            kmpProjectStructure.projectDependencies,
            kmpProjectStructure.projectsKmpGraph,
            output
        )
    }

    private fun buildGraphvizOutput(
        root: String,
        projectsHierarchy: Map<String, Set<String>>,
        projectGraphs: Map<String, Map<KmpSourceNode, List<KmpSourceNode>>>,
        output: File
    ) {
        val clusters = mutableListOf<Graph>()
        val clusterLinks = mutableMapOf<String, String>()
        val queue = mutableListOf<String>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val links = projectGraphs.getValue(current)
                .flatMap { (root, children) ->
                    children.map { child ->
                        val from = createNode(current, root)
                        val to = createNode(current, child)
                        from.link(to)
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
                    createClusterConnection(root, d, clusterLinks)
                }
            })

        Graphviz.fromGraph(g).render(Format.PNG).toFile(output)
    }

    private fun findDeepestLeaf(graph: Map<KmpSourceNode, List<KmpSourceNode>>): String {
        var res = graph.keys.first { it.name == sourceSetType.value }
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
    kmpProjectStructure: KmpProjectStructure
): Renderer {
    return GraphvizRenderer(kmpProjectStructure, SourceSetType.MAIN)
}

