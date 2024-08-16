package com.kmp.explorer.internal

import com.kmp.explorer.external.SourceSetType
import com.kmp.explorer.internal.render.createRenderer
import guru.nidi.graphviz.engine.Format
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File


class GraphvizRendererTest {

    @Test
    fun run_main_graph_creation() {
        val format = Format.DOT
        val expected = File("src/test/resources/out.dot")
        val actual = File.createTempFile("out", ".${format.fileExtension}")
        val grahvizRenderer = createRenderer(
            StubProjectBuilder.buildSimpleKmpProject(),
            SourceSetType.MAIN,
            format
        )
        grahvizRenderer.render(actual)

        assertEquals(
            expected.readText(),
            actual.readText()
        )
    }
}