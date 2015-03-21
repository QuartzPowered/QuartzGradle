package net.minecrell.quartz.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class QuartzPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.logger.info("Test")
    }

}
