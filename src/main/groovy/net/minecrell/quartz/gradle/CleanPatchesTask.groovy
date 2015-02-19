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
