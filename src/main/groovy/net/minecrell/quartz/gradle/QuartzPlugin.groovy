package net.minecrell.quartz.gradle

import net.minecrell.quartz.gradle.tasks.DeobfuscateJar
import net.minecrell.quartz.gradle.tasks.DownloadServerTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip

import java.nio.file.Path

class QuartzPlugin implements Plugin<Project> {

    Path quartzPath
    Path minecraftPath

    Project project

    @Override
    void apply(Project project) {
        this.project = project

        project.with {
            quartzPath = gradle.gradleUserHomeDir.toPath().resolve('caches').resolve('quartz')
            minecraftPath = quartzPath.resolve('minecraft')

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

                evaluationDependsOn ':mc'

                task('deobfuscateJar', type: DeobfuscateJar) {
                    dependsOn filterJar
                    input = filterJar.archivePath
                    mappings = configurations.mappings
                    output = minecraftPath.resolve("minecraft_server-$quartz.minecraft-deobfuscated.jar").toFile()
                }
            }
        }
    }

}
