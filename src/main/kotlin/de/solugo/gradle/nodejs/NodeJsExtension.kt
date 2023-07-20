package de.solugo.gradle.nodejs

import org.gradle.api.Project
import org.gradle.process.ExecSpec
import java.io.File

class NodeJsExtension(private val project: Project) {
    val version = project.objects.property(String::class.java).convention(
        project.provider { "18.16.0" }
    )
    val cachePath = project.objects.property(File::class.java).convention(
        project.provider { project.file(System.getProperty("user.home")).resolve(".gradle/nodejs") }
    )
    val rootPath = project.objects.property(File::class.java).convention(
        project.provider { project.projectDir }
    )

    fun version(value: Any?) {
        version.set(value?.toString())
    }

    fun cachePath(value: Any?) {
        cachePath.set(value?.let { project.file(it) } )
    }

    fun rootPath(value: Any?) {
        rootPath.set(value?.let { project.file(it) } )
    }

    val instance
        get() = NodeJsRegistry.resolve(
            version = version.get(),
            cacheFolder = cachePath.get(),
            onInstall = { version, folder ->
                project.logger.lifecycle("Installing Node.js v$version to $folder")
            }
        )

    fun exec(action: NodeJsExecSpec.() -> Unit) {
        project.exec { spec ->
            spec.prependPath(instance.binFolder.absolutePath)
            spec.workingDir = rootPath.get()
            action(object : NodeJsExecSpec, ExecSpec by spec {
                override fun resolveBinary(name: String) = resolve(
                    "binary '$name'",
                    instance.binFolder,
                    workingDir.resolve("node_modules").resolve(".bin"),
                ) {
                    when (NodeJsRegistry.platform) {
                        NodeJsRegistry.Platform.WINDOWS -> it.name == "$name.cmd" || it.name == "$name.exe"
                        else -> it.name == "$name.sh" || it.name == name
                    }
                }

                override fun resolveScript(name: String) = resolve(
                    "script '$name'",
                    workingDir,
                    workingDir.resolve("node_modules"),
                    project.projectDir,
                ) {
                    it.name == name
                }

                private fun resolve(
                    target: String,
                    vararg folders: File,
                    predicate: (File) -> Boolean,
                ) = folders.firstNotNullOfOrNull {
                    it.listFiles()?.firstOrNull(predicate)?.takeIf { it.exists() }?.absolutePath
                } ?: error("Could not resolve $target in ${folders.joinToString(prefix = "[", postfix = "]")}")
            })
        }
    }

    fun execBinary(binary: String, vararg args: String) {
        exec {
            commandLine(
                buildList {
                    add(resolveBinary(binary))
                    addAll(args)
                }
            )
        }
    }

    fun execScript(script: String, vararg args: String) {
        exec {
            commandLine(
                buildList {
                    add(resolveScript(script))
                    addAll(args)
                }
            )
        }
    }


    fun execNode(vararg args: String) {
        execNode {
            commandLine(*args)
        }
    }

    fun execNode(action: NodeJsExecSpec.() -> Unit) {
        exec {
            action()
            commandLine = buildList {
                add(resolveBinary("node"))
                addAll(commandLine)
            }
        }
    }

    fun execNpm(vararg args: String) {
        execNpm {
            commandLine(*args)
        }
    }

    fun execNpm(action: NodeJsExecSpec.() -> Unit) {
        exec {
            action()
            commandLine = buildList {
                add(resolveBinary("npm"))
                addAll(commandLine)
            }
        }
    }

    fun execNpx(vararg args: String) {
        execNpx {
            commandLine(*args)
        }
    }

    fun execNpx(action: NodeJsExecSpec.() -> Unit) {
        exec {
            action()
            commandLine = buildList {
                add(resolveBinary("npx"))
                addAll(commandLine)
            }
        }
    }

    interface NodeJsExecSpec : ExecSpec {
        fun resolveBinary(name: String): String

        fun resolveScript(name: String): String
    }

    private fun ExecSpec.prependPath(vararg paths: String) {
        val key = environment.keys.firstOrNull { it.equals("path", ignoreCase = true) } ?: "PATH"
        val old = environment[key]?.toString()?.split(File.pathSeparator) ?: emptyList()
        val new = buildList {
            addAll(paths)
            addAll(old)
        }

        environment[key] = new.joinToString(File.pathSeparator)
    }

}