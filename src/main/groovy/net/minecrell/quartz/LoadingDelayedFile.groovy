package net.minecrell.quartz

import net.minecraftforge.gradle.common.BaseExtension
import net.minecraftforge.gradle.common.BasePlugin
import net.minecraftforge.gradle.delayed.DelayedFile

class LoadingDelayedFile extends DelayedFile {

    private final Closure loader

    LoadingDelayedFile(BasePlugin<? extends BaseExtension> owner, String pattern, Closure loader) {
        super(owner.project, pattern, owner)
        this.loader = loader
    }

    @Override
    File resolveDelayed() {
        def result = super.resolveDelayed()
        loader.call result
        result
    }

}
