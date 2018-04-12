package de.solugo.gradle.nodejs.webpack

import de.solugo.gradle.nodejs.NodeJsPlugin
import de.solugo.gradle.nodejs.NodeJsTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class WebpackPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply(NodeJsPlugin)

        project.tasks.create(name: "webpack", type: NodeJsTask) {
            dependsOn "npmInstall"
            require = ["webpack", "webpack-cli"]
            executable = "webpack-cli"
            args = ["--mode=production"]
        }

        project.tasks.create(name: "webpackWatch", type: NodeJsTask) {
            dependsOn "npmInstall"
            require = ["webpack", "webpack-cli"]
            executable = "webpack-cli"
            args = ["--mode=development", "--watch"]
        }

        if (project.tasks.processResources != null) {
            project.tasks.processResources.dependsOn "webpack"
        }
    }

}
