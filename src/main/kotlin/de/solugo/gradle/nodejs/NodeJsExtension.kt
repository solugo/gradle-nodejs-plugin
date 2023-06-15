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
            spec.workingDir = rootPath.get()
            action(object : NodeJsExecSpec, ExecSpec by spec {
                override fun resolveBinary(name: String) = resolve(
                    "binary '$name'",
                    instance.binFolder,
                    instance.modulesFolder.resolve(".bin"),
                    workingDir.resolve("node_modules").resolve(".bin"),
                ) {
                    it.isFile && when (NodeJsRegistry.platform) {
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
                    it.listFiles()?.firstOrNull(predicate)?.absolutePath
                } ?: error("Could not resolve $target in ${folders.joinToString(prefix = "[", postfix = "]")}")
            })
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

    fun execNpm(action: NodeJsExecSpec.() -> Unit) {
        exec {
            action()
            commandLine = buildList {
                add(resolveBinary("npm"))
                addAll(commandLine)
            }
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


}