import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("com.gradle.plugin-publish") version "1.2.0"
}

group = "de.solugo.gradle"

repositories {
    mavenCentral()
}

dependencies {
    val gradleTestVersion = "1.0.3"
    val junitVersion = "5.8.1"

    implementation(gradleApi())
    implementation("org.apache.commons:commons-compress:1.23.0")

    testImplementation(gradleTestKit())
    testImplementation("de.solugo.gradle.test:gradle-test-core:$gradleTestVersion")
    testImplementation("org.assertj:assertj-core:3.21.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

gradlePlugin {
    plugins {
        create("nodeJsPlugin") {
            id = "de.solugo.nodejs"
            implementationClass = "de.solugo.gradle.nodejs.NodeJsPlugin"
            displayName = "Gradle NodeJs plugin"
            description = "Plugin for Node.js support in gradle"
            website.set("https://github.com/solugo/gradle-nodejs-plugin")
            vcsUrl.set("https://github.com/solugo/gradle-nodejs-plugin.git")
            tags.set(listOf("nodejs", "npm"))
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED)
        showStandardStreams = true
        showStackTraces = true
        showCauses = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}
