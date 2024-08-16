package com.kmp.explorer

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val CONFIGURATION_CACHE_REUSED = "Reusing configuration cache."

class KmpExplorerE2ETest {

    @TempDir
    lateinit var projectDir: File

    @BeforeEach
    fun before() {
        setupProjectDir()
    }

    @Test
    fun `task executed succesfully`() {
        val firstRun = runKmpExplorerTask()
        assertEquals(
            firstRun.tasks.first { it.path.contains("exploreMainGraph") }.outcome,
            TaskOutcome.SUCCESS)
    }

    @Test
    fun `second run is cached`() {
        runKmpExplorerTask()
        val secondRun = runKmpExplorerTask()

        assertEquals(
            secondRun.tasks.first { it.path.contains("exploreMainGraph") }.outcome,
            TaskOutcome.UP_TO_DATE
        )
    }

    @Test
    fun `second run triggers configuration cache`() {
        runKmpExplorerTask()
        val secondRun = runKmpExplorerTask()

        assertTrue(secondRun.output.contains(CONFIGURATION_CACHE_REUSED))
    }

    private fun runKmpExplorerTask(): BuildResult {
        return GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("composeApp:exploreGraph")
            .build()
    }

    private fun setupProjectDir() {
        val resources = File("src/e2eTest/resources/kmp")
        resources.copyRecursively(projectDir)
    }
}