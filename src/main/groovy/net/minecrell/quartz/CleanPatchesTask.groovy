package net.minecrell.quartz

import com.github.abrarsyed.jastyle.FileWildcardFilter
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.TaskAction

class CleanPatchesTask extends AbstractTask {

    Closure<File> patchDir

    @TaskAction
    void doTask() throws IOException {
        // YES, this is a bit hacky, but it works :P
        def patchDir = getPatchDir()
        if (patchDir.directory) {
            patchDir.listFiles(new FileWildcardFilter('*.patch*')).each {
                if (it.name.startsWith('net.minecraft.client')) {
                    it.delete()
                } else {
                    def found = false
                    def num = 0
                    def lines = it.readLines().findAll {
                        def line = it.trim()
                        if (line.startsWith("@SideOnly(Side.CLIENT)")) {
                            found = true
                            num = it.indexOf('@')
                            return false
                        } else if (found) {
                            if (line.startsWith("}") && it.indexOf('}') == num) {
                                found = false
                            }

                            return false
                        }

                        true
                    }
                    it.withWriter { w ->
                        lines.each {
                            w.write(it)
                            w.write('\n')
                        }
                    }
                }
            }
        }
    }

    File getPatchDir() {
        patchDir.call()
    }
}
