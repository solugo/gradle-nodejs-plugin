package de.solugo.gradle.nodejs

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.compress.utils.IOUtils
import org.apache.tools.ant.taskdefs.condition.Os
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap

object NodeJsRegistry {

    private val instances = ConcurrentHashMap<Key, Instance>()

    val platform = when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> Platform.WINDOWS
        Os.isFamily(Os.FAMILY_UNIX) -> Platform.LINUX
        Os.isFamily(Os.FAMILY_MAC) -> Platform.MAC
        else -> throw UnsupportedOperationException("Platform not supported")
    }

    val architecture = when {
        Os.isArch("amd64") || Os.isArch("x86_64") -> Architecture.X64
        Os.isArch("x86") -> Architecture.X86
        else -> throw UnsupportedOperationException("Architecture not supported")
    }

    fun resolve(
        version: String,
        cacheFolder: File,
        onInstall: ((version: String, folder: File) -> Unit)? = null,
    ) = instances.computeIfAbsent(Key(version = version)) {
        val installFolder = cacheFolder.resolve("v$version")

        val compression = when (platform) {
            Platform.WINDOWS -> Compression.ZIP
            Platform.LINUX -> Compression.TAR_XZ
            Platform.MAC -> Compression.TAR_GZ
        }

        if (!installFolder.exists()) {
            onInstall?.invoke(version, installFolder)

            val filename = "node-v$version-${platform.value}-${architecture.value}"

            URL("https://nodejs.org/dist/v$version/$filename.${compression.value}").openStream().let {
                when (compression) {
                    Compression.ZIP -> ZipArchiveInputStream(it)
                    Compression.TAR_XZ -> TarArchiveInputStream(XZCompressorInputStream(it))
                    Compression.TAR_GZ -> TarArchiveInputStream(GzipCompressorInputStream(it))
                }
            }.use { inputStream ->
                while (true) {
                    val entry = inputStream.nextEntry ?: break
                    val destination = installFolder.resolve(entry.name.removePrefix("/").removePrefix("$filename/"))

                    when {
                        entry.isDirectory -> {
                            destination.mkdirs()
                        }

                        entry is TarArchiveEntry && entry.isSymbolicLink -> {
                            Files.createSymbolicLink(
                                destination.toPath(),
                                destination.parentFile.resolve(entry.linkName).toPath(),
                            )
                        }

                        else -> {
                            destination.outputStream().use { IOUtils.copy(inputStream, it) }
                        }
                    }

                    if (entry is TarArchiveEntry) {
                        destination.setExecutable(entry.mode and "111".toInt(radix = 8) != 0)
                        destination.setWritable(entry.mode and "222".toInt(radix = 8) != 0)
                        destination.setReadable(entry.mode and "444".toInt(radix = 8) != 0)
                    }
                }
            }
        }

        val modulesFolder = installFolder.walk().firstOrNull { it.name == "node_modules" } ?: error(
            "Could not find node_modules folder in $installFolder"
        )

        Instance(
            version = version,
            modulesFolder = modulesFolder,
            installFolder = installFolder,
            binFolder = installFolder.resolve("bin").takeIf { it.exists() } ?: installFolder,
        )
    }

    enum class Platform(val value: String) {
        WINDOWS("win"), LINUX("linux"), MAC("darwin")
    }

    enum class Architecture(val value: String) {
        X86("x86"), X64("x64"), ARMv7("armv7l"), ARMv8("arm64")
    }

    private enum class Compression(val value: String) {
        ZIP("zip"), TAR_GZ("tar.gz"), TAR_XZ("tar.xz")
    }

    private data class Key(
        val version: String
    )

    data class Instance(
        val version: String,
        val modulesFolder: File,
        val installFolder: File,
        val binFolder: File,
    )
}