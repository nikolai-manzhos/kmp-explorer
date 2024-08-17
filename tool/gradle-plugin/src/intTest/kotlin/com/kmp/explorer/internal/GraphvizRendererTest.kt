package com.kmp.explorer.internal

import com.kmp.explorer.external.SourceSetType
import com.kmp.explorer.internal.render.createRenderer
import guru.nidi.graphviz.engine.Format
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File


class GraphvizRendererTest {

    @Test
    fun `render single project`() {
        val actual = renderGraph(StubProjectBuilder.buildSingleKmpProject())
        val expected = File("src/intTest/resources/out_single.dot")
        assertEquals(
            expected.readText(),
            actual.readText()
        )
    }

    @Test
    fun `render multiple projects`() {
        val actual = renderGraph(StubProjectBuilder.buildKmpProjectWithDeps())
        val expected = File("src/intTest/resources/out_deps.dot")
        assertEquals(
            expected.readText(),
            actual.readText()
        )
    }

    private fun renderGraph(projectStructure: KmpProjectStructure): File {
        val format = Format.DOT
        val output = File.createTempFile("out", ".${format.fileExtension}")
        val grahvizRenderer = createRenderer(
            projectStructure,
            SourceSetType.MAIN,
            format
        )
        grahvizRenderer.render(output)
        return output
    }
}