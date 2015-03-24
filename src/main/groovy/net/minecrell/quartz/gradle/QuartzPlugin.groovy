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

import net.minecrell.quartz.gradle.tasks.DeobfuscateJar
import net.minecrell.quartz.gradle.tasks.DownloadServerTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

import java.nio.file.Path

class QuartzPlugin implements Plugin<Project> {

    Path quartzPath
    Path minecraftPath

    Path projectQuartzPath
    Path projectMinecraftPath

    Project project

    @Override
    void apply(Project project) {
        this.project = project

        project.with {
            quartzPath = gradle.gradleUserHomeDir.toPath().resolve('caches').resolve('quartz')
            minecraftPath = quartzPath.resolve('minecraft')

            projectQuartzPath = project.buildDir.toPath().resolve('quartz')
            projectMinecraftPath = projectQuartzPath.resolve('minecraft')

            extensions.create('quartz', QuartzExtension)
            configurations.create('mappings')

            repositories {
                maven {
                    name = 'minecraft'
                    url = 'https://libraries.minecraft.net'
                }
            }

            afterEvaluate {
                minecraftPath = minecraftPath.resolve(quartz.minecraft)

                task('downloadServer', type: DownloadServerTask) {
                    url = QuartzConstants.getServerUrl(quartz.minecraft)
                    output = minecraftPath.resolve(QuartzConstants.getServerFilename(quartz.minecraft)).toFile()
                }

                task('filterJar', type: Zip, dependsOn: downloadServer) {
                    from zipTree(downloadServer.output)

                    baseName = 'minecraft_server'
                    version = quartz.minecraft
                    classifier = 'filtered'
                    extension = 'jar'
                    destinationDir = minecraftPath.toFile()

                    include '*.*'
                    include 'META-INF/**'
                    include 'assets/**'
                    include 'net/minecraft/**'
                }

                task('deobfuscateJar', type: DeobfuscateJar) {
                    dependsOn filterJar
                    input = filterJar.archivePath
                    mappings = configurations.mappings
                    output = projectMinecraftPath.resolve("minecraft_server-$quartz.minecraft-deobfuscated.jar").toFile()
                }
            }
        }
    }

}
