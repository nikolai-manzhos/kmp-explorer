package com.kmp.explorer.internal

internal object StubProjectBuilder {

    fun buildSimpleKmpProject(): KmpProjectStructure {
        val projectDependencies = mapOf(
            "app" to setOf("kmp-lib-1"),
            "kmp-lib-1" to setOf("kmp-lib-2")
        )
        val kmpGraphs = mapOf(
            "app" to createSimpleKmpGraph(),
            "kmp-lib-1" to createSimpleKmpGraph(),
            "kmp-lib-2" to createSimpleKmpGraph()
        )
        return KmpProjectStructure("app", projectDependencies, kmpGraphs)
    }

    private fun createSimpleKmpGraph(): Map<KmpSourceNode, List<KmpSourceNode>> {
        val androidMain = KmpSourceNode("androidMain", true)
        val linuxMain = KmpSourceNode("linuxMain", false)
        return mapOf(
            KmpSourceNode("commonMain", true) to listOf(androidMain, linuxMain),
            linuxMain to emptyList(),
            androidMain to emptyList()
        )
    }
}