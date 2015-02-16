package net.minecrell.ice.gradle

import net.minecraftforge.gradle.user.UserBasePlugin
import net.minecraftforge.gradle.user.UserExtension

class IceExtension extends UserExtension {

    String tweaker = 'null'

    IceExtension(UserBasePlugin<? extends UserExtension> plugin) {
        super(plugin)
    }

}
