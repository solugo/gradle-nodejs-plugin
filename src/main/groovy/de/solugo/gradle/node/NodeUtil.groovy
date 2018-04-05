package de.solugo.gradle.node

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.compress.utils.IOUtils
import org.apache.tools.ant.taskdefs.condition.Os

class NodeUtil {

    static synchronized NodeUtil getInstance(String version) {
        File home = new File(System.getProperty("user.home"))
        File cache = new File(home, ".node")
        File target = new File(cache, version)

        File executable
        File modules


        final String platform
        final String arch
        final String ext

        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            platform = "win"
            ext = "zip"
            executable = new File(target, "node.exe")
            modules = new File(target, "node_modules")
        } else if (Os.isFamily(Os.FAMILY_UNIX)) {
            platform = "linux"
            ext = "tar.xz"
            executable = new File(target, "bin/node")
            modules = new File(target, "lib/node_modules")
        } else if (Os.isFamily(Os.FAMILY_MAC)) {
            platform = "darwin"
            ext = "tar.gz"
            executable = new File(target, "bin/npm")
            modules = new File(target, "lib")
        } else {
            throw new UnsupportedOperationException("Platform not supported")
        }

        if (Os.isArch("amd64")) {
            arch = "x64"
        } else if (Os.isArch("x86")) {
            arch = "x86"
        } else {
            throw new UnsupportedOperationException("Architecture not supported")
        }

        if (!target.exists()) {
            URL url = new URL("https://nodejs.org/dist/v${version}/node-v${version}-${platform}-${arch}.${ext}")

            println("Downloading ${url} to ${target}")

            if (target.isDirectory()) {
                target.delete()
            }
            target.mkdirs()

            try {
                final ArchiveStreamFactory streamFactory = new ArchiveStreamFactory()

                url.withInputStream {
                    if (url.toString().endsWith("tar.xz")) {
                        extract(
                                streamFactory.createArchiveInputStream(
                                        new BufferedInputStream(new XZCompressorInputStream(it))
                                ),
                                target
                        )
                    } else if (url.toString().endsWith("zip")) {
                        extract(
                                streamFactory.createArchiveInputStream(
                                        new BufferedInputStream(it)
                                ),
                                target
                        )
                    } else {
                        throw new UnsupportedOperationException("Archive ${url} not supported")
                    }

                }
            } catch (final Throwable throwable) {
                target.delete()
                throw throwable
            }

        }

        return new NodeUtil(target, executable, modules)
    }

    private static void extract(final ArchiveInputStream inputStream, final File folder) {
        ArchiveEntry entry
        while ((entry = inputStream.nextEntry) != null) {
            final File target = new File(folder, entry.name.substring(entry.name.indexOf("/") + 1))
            if (entry.directory) {
                target.mkdirs();
            } else {
                target.parentFile.mkdirs()
                target.withOutputStream {
                    IOUtils.copy(inputStream, it)
                }

                if (entry instanceof TarArchiveEntry) {
                    final TarArchiveEntry tarArchiveEntry = (TarArchiveEntry) entry
                    if (tarArchiveEntry.mode && 00100 > 0) {
                        target.executable = true
                    }
                }

            }
        }
    }

    private final File modules
    private final File home
    private final File executable

    private NodeUtil(final File home, final File executable, final File modules) {
        this.modules = modules
        this.home = home
        this.executable = executable
    }

    File getModules() {
        return this.modules
    }

    File getHome() {
        return this.modules
    }

    File getExecutable() {
        return this.executable
    }

}
