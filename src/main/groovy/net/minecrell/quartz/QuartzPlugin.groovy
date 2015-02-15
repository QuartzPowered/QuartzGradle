package net.minecrell.quartz

import static net.minecraftforge.gradle.common.Constants.JAR_SERVER_FRESH
import static net.minecraftforge.gradle.user.UserConstants.MCP_PATCH_DIR
import static net.minecraftforge.gradle.user.UserConstants.MERGE_CFG

import net.minecraftforge.gradle.delayed.DelayedFile
import net.minecraftforge.gradle.tasks.ProcessJarTask
import net.minecraftforge.gradle.user.UserBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

class QuartzPlugin extends UserBasePlugin<QuartzExtension> {

    private static final String JAR_SERVER =
            '{CACHE_DIR}/minecraft/net/minecraft/minecraft_server/{MC_VERSION}/minecraft_server_filtered-{MC_VERSION}.jar'

    private static final String FML_VERSION = '1.8-8.0.27.1027'
    private static final String QUARTZ_CACHE_DIR = "{CACHE_DIR}/minecraft/net/minecrell/quartz/$FML_VERSION"

    @Override
    protected Class<QuartzExtension> getExtensionClass() {
        QuartzExtension
    }

    @Override
    void applyPlugin() {
        super.applyPlugin()

        project.with {
            tasks.mergeJars.enabled = false

            task('filterJar', type: FilterJarTask) {
                mergeCfg = delayedFile MERGE_CFG
                inJar = delayedFile JAR_SERVER_FRESH
                outJar = delayedFile JAR_SERVER
            }

            configure([tasks.deobfBinJar, tasks.deobfuscateJar]) {
                dependsOn.with {
                    remove 'mergeJars'
                    add 'filterJar'
                }
                inJar = delayedFile JAR_SERVER
            }

            task('cleanPatches', type: CleanPatchesTask) {
                patchDir = delayedFile MCP_PATCH_DIR
            }

            tasks.decompile.dependsOn 'cleanPatches'
        }
    }

    @Override
    protected void delayedTaskConfig() {
        ProcessJarTask binDeobf = project.tasks.deobfBinJar
        ProcessJarTask decompDeobf = project.tasks.deobfuscateJar

        JavaPluginConvention java = project.convention.plugins['java']
        SourceSet main = java.sourceSets.main

        main.resources.files.each {
            if (it.name.endsWith('_at.cfg')) {
                project.getLogger().lifecycle("Found AccessTransformer in main resources: $it.name")
                binDeobf.addTransformer(it)
                decompDeobf.addTransformer(it)
            }
        }

        super.delayedTaskConfig()
    }

    @Override
    String getApiName() {
        'minecraft_server'
    }

    @Override
    protected String getSrcDepName() {
        'minecraft_server_src'
    }

    @Override
    protected String getBinDepName() {
        'minecraft_server_bin'
    }

    @Override
    protected boolean hasApiVersion() {
        false
    }

    @Override
    protected String getApiVersion(QuartzExtension ext) {
        null
    }

    @Override
    protected String getMcVersion(QuartzExtension ext) {
        ext.version
    }

    @Override
    protected String getApiCacheDir(QuartzExtension ext) {
        '{BUILD_DIR}/minecraft/net/minecraft/minecraft_server/{MC_VERSION}'
    }

    @Override
    protected String getSrgCacheDir(QuartzExtension userExtension) {
        "$QUARTZ_CACHE_DIR/srgs"
    }

    @Override
    protected String getUserDevCacheDir(QuartzExtension userExtension) {
        "$QUARTZ_CACHE_DIR/unpacked"
    }

    @Override
    protected String getUserDev() {
        "net.minecraftforge:fml:$FML_VERSION"
    }

    @Override
    protected String getClientTweaker() {
        ''
    }

    @Override
    protected String getServerTweaker() {
        project.minecraft.tweaker
    }

    @Override
    protected String getStartDir() {
        '{BUILD_DIR}/start/QuartzStart'
    }

    @Override
    protected String getClientRunClass() {
        'net.minecraft.client.main.Main'
    }

    @Override
    protected Iterable<String> getClientRunArgs() {
        def result = ['--noCoreSearch']
        def args = project.properties['runArgs']
        if (args != null) {
            result << args
        }

        result
    }

    @Override
    protected String getServerRunClass() {
        'net.minecraft.launchwrapper.Launch'
    }

    @Override
    protected Iterable<String> getServerRunArgs() {
        getClientRunArgs()
    }

    @Override
    protected void configureDeobfuscation(ProcessJarTask processJarTask) {

    }

    @Override
    protected void doVersionChecks(String s) {

    }

    @Override
    void applyOverlayPlugin() {

    }

    @Override
    boolean canOverlayPlugin() {
        false
    }

    @Override
    protected DelayedFile getDevJson() {
        new LoadingDelayedFile(this, "$QUARTZ_CACHE_DIR/unpacked/dev.json", { File file ->
            if (file.exists()) {
                file.withOutputStream { o ->
                    QuartzPlugin.getResourceAsStream('/1.8.json').withStream {
                        o << it
                    }
                }
            }
        })
    }

    @Override
    protected QuartzExtension getOverlayExtension() {
        null
    }

}
