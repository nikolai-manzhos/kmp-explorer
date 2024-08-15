package com.kmp.explorer.internal

import java.io.Serializable

internal class KmpProjectStructure(
    val rootProjectPath: String,
    val projectDependencies: Map<String, Set<String>>,
    val projectsKmpGraph: Map<String, Map<KmpSourceNode, List<KmpSourceNode>>>,
) : Serializable