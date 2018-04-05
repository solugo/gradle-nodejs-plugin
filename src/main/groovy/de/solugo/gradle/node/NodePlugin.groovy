package de.solugo.gradle.node

import org.gradle.api.Plugin
import org.gradle.api.Project

class NodePlugin implements Plugin<Project> {

    @Override
    void apply(final Project project) {
        project.extensions.create("node", NodeExtension)

        project.node.aliases["npm"] = "npm/bin/npm-cli.js"

        project.ext.NodeTask = NodeTask

        project.tasks.clean.doLast {
            delete project.file("node_modules")
        }

        project.tasks.create("npmInstall", NodeTask).doFirst {
            executable "npm"
            args "install"

            final def args = project.getProperties().get(NodeTask.PROPERTY_ARGS)
            if (project.file("node_modules").exists() && args == null) {
                return "SKIPPED"
            }
        }

        project.tasks.addRule("Pattern: npm<task>") { String taskName ->
            if (taskName.startsWith("npm")) {
                final String target = (taskName - "npm")
                project.tasks.create(taskName, NodeTask).doFirst {
                    executable = target.substring(0, 1).toLowerCase() + target.substring(1)
                }
            }
        }

        project.tasks.addRule("Pattern: node<task>") { String taskName ->
            if (taskName.startsWith("node")) {
                final String target = (taskName - "node")
                project.tasks.create(taskName, NodeTask).doFirst {
                    executable = target.substring(0, 1).toLowerCase() + target.substring(1)
                }
            }
        }
    }

}

