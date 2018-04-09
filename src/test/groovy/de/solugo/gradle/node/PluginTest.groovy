package de.solugo.gradle.node

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PluginTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Test
    void test() {
        final Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply('de.solugo.gradle.node')

        project.task("help", type: NodeTask) {
            args = ["--version"]
        }
    }
}
