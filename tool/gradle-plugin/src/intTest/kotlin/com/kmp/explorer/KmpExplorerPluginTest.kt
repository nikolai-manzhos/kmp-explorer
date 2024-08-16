package com.kmp.explorer

import org.gradle.api.internal.plugins.PluginApplicationException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KmpExplorerPluginTest {

    @Test
    fun `plugin creates tasks for kmp project`() {
        val parent = ProjectBuilder.builder().withName("root").build()
        val kmpProject = ProjectBuilder.builder()
            .withName("kmp")
            .withParent(parent)
            .build()
        parent.pluginManager.apply("com.kmp.explorer")
        kmpProject.pluginManager.apply("org.jetbrains.kotlin.multiplatform")

        assertTrue(kmpProject.tasks.getByName("exploreMainGraph") is KmpExplorerTask)
        assertTrue(kmpProject.tasks.getByName("exploreTestGraph") is KmpExplorerTask)
        assertNotNull(kmpProject.tasks.getByName("exploreGraph"))
    }

    @Test
    fun `plugin ignores subprojects without kmp plugin`() {
        val parent = ProjectBuilder.builder().withName("root").build()
        parent.pluginManager.apply("com.kmp.explorer")
        val jvm = ProjectBuilder.builder()
            .withParent(parent)
            .withName("jvm")
            .build()
        jvm.pluginManager.apply("application")

        assertNull(jvm.tasks.firstOrNull { it.name == "exploreGraph"})
    }

    @Test
    fun `plugin applied on subproject`() {
        val parent = ProjectBuilder.builder().withName("root").build()
        val subproject = ProjectBuilder.builder()
            .withParent(parent)
            .withName("jvm")
            .build()

        assertThrows<PluginApplicationException>(KmpExplorerPlugin.WRONG_PROJECT_MSG) {
            subproject.pluginManager.apply("com.kmp.explorer")
        }
    }
}