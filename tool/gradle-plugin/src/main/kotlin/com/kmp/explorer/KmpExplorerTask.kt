package com.kmp.explorer

import com.kmp.explorer.internal.KmpProjectStructure
import com.kmp.explorer.internal.render.createRenderer
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

internal abstract class KmpExplorerTask : DefaultTask() {

    @Input
    val kmpProjectStructureProperty: Property<KmpProjectStructure> =
        project.objects.property(KmpProjectStructure::class.java)

    @OutputFile
    val hierarchyOutput: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun run() {
        val kmpProjectStructure = kmpProjectStructureProperty.get()
        val renderer = createRenderer(kmpProjectStructure)
        renderer.render(hierarchyOutput.asFile.get())
    }
}