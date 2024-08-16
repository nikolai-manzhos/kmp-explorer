package com.kmp.explorer

import com.kmp.explorer.external.KmpExplorerExtension
import com.kmp.explorer.external.SourceSetType
import com.kmp.explorer.internal.KmpProjectStructure
import com.kmp.explorer.internal.render.createRenderer
import guru.nidi.graphviz.engine.Format
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

internal abstract class KmpExplorerTask : DefaultTask() {

    @Input
    val formatProperty: Property<Format> =
        project.objects.property(Format::class.java)

    @Input
    val sourceSetTypeProperty: Property<SourceSetType> =
        project.objects.property(SourceSetType::class.java)

    @Input
    val projectStructureProperty: Property<KmpProjectStructure> =
        project.objects.property(KmpProjectStructure::class.java)

    @OutputFile
    val hierarchyOutput: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun run() {
        val kmpProjectStructure = projectStructureProperty.get()
        val renderer = createRenderer(
            kmpProjectStructure,
            sourceSetTypeProperty.get(),
            formatProperty.get()
        )
        renderer.render(hierarchyOutput.asFile.get())
    }
}