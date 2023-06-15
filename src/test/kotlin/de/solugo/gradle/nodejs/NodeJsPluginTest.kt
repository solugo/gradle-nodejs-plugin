package de.solugo.gradle.nodejs

import de.solugo.gradle.test.core.Directory.Helper.extractDirectoryFromClasspath
import de.solugo.gradle.test.core.Directory.Helper.file
import de.solugo.gradle.test.core.Directory.Helper.withTemporaryDirectory
import de.solugo.gradle.test.core.Executor.Companion.execute
import de.solugo.gradle.test.core.GradleTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NodeJsPluginTest {

    @Test
    fun `get node version`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("default")
                path.extractDirectoryFromClasspath("empty")
            }
            execute("nodeVersion") {
                assertThat(output).contains(
                    """
                    v18.16.0
                    """.trimIndent()
                )
            }
        }
    }

    @Test
    fun `get npm version`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("default")
                path.extractDirectoryFromClasspath("empty")
            }
            execute("npmVersion") {
                assertThat(output).contains(
                    """
                    9.5.1
                    """.trimIndent()
                )
            }
        }
    }

    @Test
    fun `get yarn version`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("default")
                path.extractDirectoryFromClasspath("empty")
            }
            execute("yarnVersion") {
                assertThat(output).contains(
                    """
                    1.22.19
                    """.trimIndent()
                )
            }
        }
    }

    @Test
    fun `build in subfolder is successful`() {
        GradleTest {
            withTemporaryDirectory {
                path.extractDirectoryFromClasspath("default")
                path.extractDirectoryFromClasspath("project")
            }
            execute("buildPackage") {
                assertThat(output).contains(
                    """
                    Hello World
                    """.trimIndent()
                )
            }
        }
    }

}