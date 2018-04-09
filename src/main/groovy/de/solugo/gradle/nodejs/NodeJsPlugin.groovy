package de.solugo.gradle.nodejs

import org.gradle.api.Plugin
import org.gradle.api.Project

class NodeJsPlugin implements Plugin<Project> {

    @Override
    void apply(final Project project) {
        project.extensions.create("nodejs", NodeJsExtension)

        project.ext.NodeTask = NodeJsTask

        final cleanTask = project.tasks.findByPath("clean")
        if (cleanTask != null) {
            cleanTask.doLast {
                delete project.file("node_modules")
            }
        }

        final snakeCase = { String str -> str.replaceAll(/[A-Z]/) { "-" + it.toLowerCase() }.substring(1) }

        project.tasks.addRule("Pattern: npmRun<script>") { String taskName ->
            if (project.tasks.findByPath(taskName) == null && taskName.startsWith("npmRun")) {
                final String target = (taskName - "npmRun")
                project.tasks.create(taskName, NodeJsTask).doFirst {
                    executable = "npm/bin/npm-cli.js"
                    if (target.length() > 0) {
                        args = ["run", snakeCase(target)]
                    } else {
                        args = ["run"]
                    }
                }
            }
        }

        project.tasks.addRule("Pattern: npmRequire<package>") { String taskName ->
            if (project.tasks.findByPath(taskName) == null && taskName.startsWith("npmRequire")) {
                final String target = (taskName - "npmRequire")
                project.tasks.create(taskName, NodeJsTask).doFirst {
                    executable = "npm"
                    modules = ["npm"]
                    if (target.length() > 0) {
                        args = ["install", snakeCase(target)]
                    }
                }
            }
        }

        project.tasks.addRule("Pattern: npm<task>") { String taskName ->
            if (project.tasks.findByPath(taskName) == null && taskName.startsWith("npm")) {
                final String target = (taskName - "npm")
                project.tasks.create(taskName, NodeJsTask).doFirst {
                    executable = "npm"
                    if (target.length() > 0) {
                        args = [snakeCase(target)]
                    }
                }
            }
        }

        project.tasks.addRule("Pattern: npx<task>") { String taskName ->
            if (project.tasks.findByPath(taskName) == null && taskName.startsWith("npx")) {
                final String target = (taskName - "npx")
                project.tasks.create(taskName, NodeJsTask).doFirst {
                    executable = "npx"
                    if (target.length() > 0) {
                        args = [snakeCase(target)]
                    }
                }
            }
        }

        project.tasks.addRule("Pattern: node<script>") { String taskName ->
            if (project.tasks.findByPath(taskName) == null && taskName.startsWith("node")) {
                final String target = (taskName - "node")
                project.tasks.create(taskName, NodeJsTask).doFirst {
                    executable = "node"
                    if (target.length() > 0) {
                        args = [snakeCase(target)]
                    }
                }
            }
        }
    }

}

