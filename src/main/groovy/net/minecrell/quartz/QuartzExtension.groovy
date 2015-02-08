package net.minecrell.quartz

import net.minecraftforge.gradle.user.UserBasePlugin
import net.minecraftforge.gradle.user.UserExtension

class QuartzExtension extends UserExtension {

    String tweaker = 'null'

    QuartzExtension(UserBasePlugin<? extends UserExtension> plugin) {
        super(plugin)
    }

}
