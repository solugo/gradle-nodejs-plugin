package de.solugo.gradle.nodejs

import org.gradle.api.Plugin
import org.gradle.api.Project

class NodeJsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = NodeJsExtension(project)
        project.extensions.add("nodejs", NodeJsExtension(project))

        project.tasks.addRule("Pattern: npm<Task>: Runs npm task") { name ->
            if (project.tasks.findByPath(name) == null && name.startsWith("npm")) {
                val target = name.removePrefix("npm").replaceFirstChar { it.lowercase() }

                project.tasks.create(name) { task ->
                    task.doFirst {
                        extension.execNpm {
                            commandLine(target)
                        }
                    }
                }
            }
        }

        project.tasks.addRule("Pattern: npmRun<Task>: Runs npm run task") { name ->
            if (project.tasks.findByPath(name) == null && name.startsWith("npmRun")) {
                val target = name.removePrefix("npmRun").replaceFirstChar { it.lowercase() }

                project.tasks.create(name) { task ->
                    task.doFirst {
                        extension.execNpm {
                            commandLine("run", target)
                        }
                    }
                }
            }
        }

        project.tasks.addRule("Pattern: yarn<Task>: Runs yarn task") { name ->
            if (project.tasks.findByPath(name) == null && name.startsWith("yarn")) {
                val target = name.removePrefix("yarn").replaceFirstChar { it.lowercase() }

                project.tasks.create(name) { task ->
                    task.doFirst {
                        extension.execNode {
                            commandLine("yarn", target)
                        }
                    }
                }
            }
        }

        project.tasks.addRule("Pattern: npx<Task>: Runs npx task") { name ->
            if (project.tasks.findByPath(name) == null && name.startsWith("npx")) {
                val target = name.removePrefix("npx").replaceFirstChar { it.lowercase() }

                project.tasks.create(name) { task ->
                    task.doFirst {
                        extension.execNpx {
                            commandLine("npx", target)
                        }
                    }
                }
            }
        }


        project.tasks.addRule("Pattern: node<Script>: Runs node script") { name ->
            if (project.tasks.findByPath(name) == null && name.startsWith("npx")) {
                val target = name.removePrefix("npx").replaceFirstChar { it.lowercase() }

                project.tasks.create(name) { task ->
                    task.doFirst {
                        extension.exec {
                            commandLine("$target.js")
                        }
                    }
                }
            }
        }

    }
}