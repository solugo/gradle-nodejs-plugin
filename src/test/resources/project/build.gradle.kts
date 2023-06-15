plugins {
    id("java")
    id("de.solugo.nodejs")
}

nodejs {
    rootPath("./package")
}

tasks.create("buildPackage") {
    doFirst {
        nodejs.execNpm("run", "hello")
    }
}