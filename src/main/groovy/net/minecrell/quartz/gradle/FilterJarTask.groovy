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
                    if (filter.find { name.startsWith(it) } == null) {
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
