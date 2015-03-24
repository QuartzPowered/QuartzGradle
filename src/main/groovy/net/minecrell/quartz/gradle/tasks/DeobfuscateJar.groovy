package net.minecrell.quartz.gradle.tasks

import net.minecrell.quartz.mappings.MappedClass
import net.minecrell.quartz.mappings.loader.Mappings
import net.minecrell.quartz.mappings.transformer.MappingsTransformer
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class DeobfuscateJar extends DefaultTask {

    @InputFiles
    FileCollection mappings

    @InputFile
    File input

    @OutputFile
    File output

    @TaskAction
    void deobfuscateJar() {
        File mappingsFile

        mappings.find {
            FileCollection tree
            if (it.directory) {
                tree = project.fileTree(it)
            } else {
                tree = project.zipTree(it)
            }

            mappingsFile = tree.find {
                it.name == 'mappings.json'
            }

            mappingsFile != null
        }

        if (mappingsFile == null) {
            throw new RuntimeException("Failed to locate mappings.json")
        }

        Map<String, MappedClass> mappings

        mappingsFile.withReader {
            mappings = Mappings.read(it)
        }

        def mapper = Mappings.createMapper(mappings)

        def zipIn = new ZipFile(input)
        zipIn.withCloseable {
            def zipOut = new ZipOutputStream(output.newOutputStream())
            zipOut.withStream {
                MappingsTransformer.deobfuscate(zipIn, zipOut, mapper)
            }
        }
    }

}
