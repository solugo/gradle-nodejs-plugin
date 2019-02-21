package de.solugo.gradle.nodejs

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarConstants
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.utils.IOUtils
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.file.FileTree

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.util.zip.ZipEntry

class NodeJsUtil {

    static synchronized NodeJsUtil getInstance(Project project, String version) {
        File home = new File(System.getProperty("user.home"))
        File cache = new File(home, ".nodejs")
        File target = new File(cache, version)

        File modules
        File bin

        final String platform
        final String arch
        final String ext

        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            platform = "win"
            ext = "zip"
            modules = new File(target, "node_modules")
            bin = target
        } else if (Os.isFamily(Os.FAMILY_MAC)) {
            platform = "darwin"
            ext = "tar.gz"
            modules = new File(target, "lib/node_modules")
            bin = new File(target, "bin")
        } else if (Os.isFamily(Os.FAMILY_UNIX)) {
            platform = "linux"
            ext = "tar.gz"
            modules = new File(target, "lib/node_modules")
            bin = new File(target, "bin")
        } else {
            throw new UnsupportedOperationException("Platform not supported")
        }

        if (Os.isArch("amd64") || Os.isArch("x86_64")) {
            arch = "x64"
        } else if (Os.isArch("x86")) {
            arch = "x86"
        } else {
            throw new UnsupportedOperationException("Architecture not supported")
        }

        if (!target.exists() || target.listFiles().size() == 0) {
            final root = "node-v${version}-${platform}-${arch}"
            final file = "${root}.${ext}"
            final downloadFile = new File(cache, file)

            if (!downloadFile.exists()) {

                downloadFile.parentFile.mkdirs()
                final url = new URL("https://nodejs.org/dist/v${version}/${file}")

                println("Downloading NodeJs from ${url}")
                url.withInputStream { is -> downloadFile.withOutputStream { os -> os << is } }
            }

            try {
                println("Extracting NodeJs to ${target.absolutePath}")

                target.mkdirs()

                final ArchiveInputStream inputStream
                if (ext == "tar.gz") {
                    inputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(downloadFile)));
                } else if (ext == "zip") {
                    inputStream = new ZipArchiveInputStream(new FileInputStream(downloadFile))
                } else {
                    throw new UnsupportedOperationException("Archive ${downloadFile.name} not supported")
                }

                ArchiveEntry entry = inputStream.nextEntry
                while (entry != null) {
                    final name = entry.name
                    final int start = name.indexOf("/")

                    if (start != -1) {
                        final destination = new File(target, name.substring(start + 1))
                        if (entry.isDirectory()) {
                            destination.mkdirs()
                        } else {
                            if (entry instanceof TarArchiveEntry) {

                                if (entry.isSymbolicLink()) {
                                    Files.createSymbolicLink(
                                            destination.toPath(),
                                            Paths.get(entry.linkName)
                                    )
                                } else {
                                    destination.withDataOutputStream {
                                        IOUtils.copy(inputStream, it)
                                    }
                                }

                                final ownerValue = "$entry.mode".substring(0,1).toInteger()
                                destination.setExecutable((ownerValue & 1) == 1)
                                destination.setWritable((ownerValue & 2) == 2)
                                destination.setReadable((ownerValue & 4) == 4)

                            } else {

                                destination.withDataOutputStream {
                                    IOUtils.copy(inputStream, it)
                                }

                            }
                        }
                    }

                    entry = inputStream.nextEntry
                }

            } catch (final Throwable throwable) {
                target.delete()
                throw throwable
            }

        }

        return new NodeJsUtil(target, bin, modules)
    }

    private final File home
    private final File bin
    private final File modules

    private NodeJsUtil(final File home, final File bin, final File modules) {
        this.modules = modules
        this.home = home
        this.bin = bin
    }

    File getModules() {
        return this.modules
    }

    File getHome() {
        return this.home
    }

    File getBin() {
        return this.bin
    }

}
