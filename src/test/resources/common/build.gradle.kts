plugins {
    id("java")
    id("de.solugo.nodejs")
}

tasks.create("nodeVersion") {
    doFirst {
        nodejs.execNode {
            commandLine("--version")
        }
    }
}

tasks.create("npmVersion") {
    doFirst {
        nodejs.execNpm {
            commandLine("--version")
        }
    }
}


tasks.create("yarnVersion") {
    doFirst {
        nodejs.execNpm {
            commandLine("install", "yarn@1.22.19")
        }
        nodejs.exec {
            commandLine(resolveBinary("yarn"), "--version")
        }
    }
}