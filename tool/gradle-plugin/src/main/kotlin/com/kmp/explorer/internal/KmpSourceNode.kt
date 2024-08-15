package com.kmp.explorer.internal

import java.io.Serializable

internal class KmpSourceNode(
    val name: String,
    var isVisible: Boolean
): Serializable {
    override fun toString(): String {
        return "KmpSourceNode(name='$name', isVisible=$isVisible)"
    }
}
