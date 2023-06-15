plugins {
    id("java")
    id("de.solugo.nodejs")
}

tasks.create("nodeVersion") {
    doFirst {
        nodejs.execNode("--version")
    }
}

tasks.create("npmVersion") {
    doFirst {
        nodejs.execNpm("--version")
    }
}


tasks.create("yarnVersion") {
    doFirst {
        nodejs.execNpm("install", "yarn@1.22.19")
        nodejs.execBinary("yarn", "--version")
    }
}

tasks.create("buildFrontend") {
    doFirst {
        nodejs.exec {
            workingDir("../package")
            args(resolveBinary("npm"), "run", "test")
        }
        nodejs.execNpm("install", "yarn@1.22.19")
        nodejs.execBinary("yarn", "--version")
    }
}