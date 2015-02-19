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
package net.minecrell.quartz.gradle

import net.minecraftforge.gradle.tasks.abstractutil.CachedTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class FilterJarTask extends CachedTask {

    @InputFile
    Closure<File> mergeCfg

    @InputFile
    Closure<File> inJar

    @OutputFile
    @CachedTask.Cached
    Closure<File> outJar

    @TaskAction
    void doTask() throws IOException {
        filter(getMergeCfg(), getInJar(), getOutJar())
    }

    private static void filter(File mergeCfg, File inFile, File outFile) {
        def filter = readConfig(mergeCfg)

        def inJar = new ZipFile(inFile)
        inJar.withCloseable {
            def outJar = new ZipOutputStream(outFile.newOutputStream())
            outJar.withStream {
                inJar.entries().each {
                    def name = it.name
                    if (filter.find {name.startsWith(it)} == null) {
                        ZipEntry newEntry = new ZipEntry(it)
                        outJar.putNextEntry newEntry
                        outJar << inJar.getInputStream(it)
                    }
                }
            }
        }
    }

    private static Set<String> readConfig(File mapFile) {
        def result = new HashSet<String>()
        mapFile.eachLine {
            if (it.startsWith("^")) {
                result << it.substring(1)
            }
        }
        result
    }

    File getMergeCfg() {
        mergeCfg.call()
    }

    File getInJar() {
        inJar.call()
    }

    File getOutJar() {
        outJar.call()
    }
}
