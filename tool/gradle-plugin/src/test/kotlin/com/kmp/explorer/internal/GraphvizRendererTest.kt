package com.kmp.explorer.internal

import com.kmp.explorer.internal.render.createRenderer
import org.junit.jupiter.api.Test
import java.io.File


class GraphvizRendererTest {

    @Test
    fun test() {
        val grahvizRenderer = createRenderer(StubProjectBuilder.buildSimpleKmpProject())
        grahvizRenderer.render(File(""))

    }
}