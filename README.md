[![Travis](https://img.shields.io/travis/solugo/gradle-nodejs-plugin.svg?style=for-the-badge)](https://travis-ci.org/solugo/gradle-nodejs-plugin)
[![License](https://img.shields.io/github/license/solugo/gradle-nodejs-plugin.svg?style=for-the-badge)](https://github.com/solugo/gradle-nodejs-plugin/blob/master/LICENSE)
[![Maven metadata URI](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/gradle/plugin/de/solugo/gradle/gradle-nodejs-plugin/maven-metadata.xml.svg?style=for-the-badge)](https://plugins.gradle.org/plugin/de.solugo.gradle.node)

# Gradle NodeJs Plugin
This plugin allows to use NodeJs script and tools via gradle. NodeJs will be downloaded automatically and reused across 
execution.

## Configuration
<pre>
node {
  version = '9.11.0'
}
</pre>

## Task rules

### Node
Run node script

<pre>
gradle node&lt;ScriptName&gt; -Pargs="..." // node &lt;script-name&gt;.js &lt;args&gt;
</pre>

### Npm
Run npm script

<pre>
gradle npm&lt;TaskName&gt; -Pargs="..." // npm &lt;task-name&gt; &lt;args&gt;
</pre>

### Yarn
Run yarn task

<pre>
gradle yarn&lt;TaskName&gt; -Pargs="..." // npm &lt;task-name&gt; &lt;args&gt;
</pre>


### Npx
Run node module

<pre>
gradle npx&lt;ModuleName&gt; -Pargs="..." // npx &lt;module-name&gt;.js &lt;args&gt;
</pre>


## Tasks

### NodeJsTask
<pre>
task("webpack", type: NodeJsTask) {
    require = ["webpack"]
    executable = "webpack"
    args = ["src/index.js"]
}
</pre>

## Examples

### Install development dependency using npm
<pre>
gradle npmInstall -Pargs="--save-dev webpack"
</pre>