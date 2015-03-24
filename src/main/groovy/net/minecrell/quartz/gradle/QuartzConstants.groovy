package net.minecrell.quartz.gradle

class QuartzConstants {

    static getServerFilename(String version) {
        "minecraft_server-${version}.jar"
    }

    static getServerUrl(String version) {
        "https://s3.amazonaws.com/Minecraft.Download/versions/$version/minecraft_server.${version}.jar"
    }

}
