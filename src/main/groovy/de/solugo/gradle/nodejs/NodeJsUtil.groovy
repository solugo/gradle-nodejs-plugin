package de.solugo.gradle.nodejs

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.file.FileTree

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

                final FileTree tree
                if (ext == "tar.gz") {
                    tree = project.tarTree(downloadFile)
                } else if (ext == "zip") {
                    tree = project.zipTree(downloadFile)
                } else {
                    throw new UnsupportedOperationException("Archive ${downloadFile.name} not supported")
                }

                tree.visit { source ->
                    final name = source.relativePath.pathString
                    final int start = name.indexOf("/")

                    if (start != -1) {
                        final destination = new File(target, name.substring(start + 1))
                        if (source.isDirectory()) {
                            destination.mkdirs()
                        } else {
                            source.copyTo(destination)
                        }
                    }
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
