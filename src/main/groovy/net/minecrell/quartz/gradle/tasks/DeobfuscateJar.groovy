/*
 * QuartzGradle
 * Copyright (c) 2015, Minecrell <https://github.com/Minecrell>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.minecrell.quartz.gradle.tasks

import net.minecrell.quartz.mappings.MappedClass
import net.minecrell.quartz.mappings.loader.Mappings
import net.minecrell.quartz.mappings.transformer.MappingsTransformer
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
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
