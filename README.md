[![Travis](https://img.shields.io/travis/solugo/gradle-nodejs-plugin.svg?style=for-the-badge)](https://travis-ci.org/solugo/gradle-nodejs-plugin)
[![License](https://img.shields.io/github/license/solugo/gradle-nodejs-plugin.svg?style=for-the-badge)](https://github.com/solugo/gradle-nodejs-plugin/blob/master/LICENSE)
[![Version](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/de/solugo/gradle/gradle-nodejs-plugin/maven-metadata.xml.svg?style=for-the-badge)](https://plugins.gradle.org/m2/de/solugo/gradle/gradle-nodejs-plugin/)

# [Gradle Node.js Plugin](https://plugins.gradle.org/plugin/de.solugo.gradle.nodejs)
This plugin allows to use [Node.js](https://nodejs.org) script and tools via gradle. NodeJs will be downloaded automatically and reused across 
execution.

## Configuration
<pre>
plugins {
    id "de.solugo.gradle.nodejs" version "..."
}

nodejs {
    version = '10.8.0'
    rootPath = 'subpath'
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
gradle yarn&lt;TaskName&gt; -Pargs="..." // yarn &lt;task-name&gt; &lt;args&gt;
</pre>


### Npx
Run node module

<pre>
gradle npx&lt;ModuleName&gt; -Pargs="..." // npx &lt;module-name&gt;.js &lt;args&gt;
</pre>


## Tasks types

### NodeJsTask
This task type enables you to create custom tasks using Node.js or common Node.js modules.
<pre>
task("webpack", type: NodeJsTask) {
    require = ["webpack", "webpack-cli"]
    executable = "webpack-cli"
    args = ["--mode=production"]
}
</pre>

## Examples

### Install development dependency using npm
<pre>
gradle npmInstall -Pargs="--save-dev webpack"
</pre>

# Gradle Webpack Plugin
This plugin allows to use [Webpack](https://webpack.js.org/) bundler via gradle. Webpack is automatically added to the processResources task.

## Configuration
<pre>
plugins {
    id "de.solugo.gradle.webpack" version "..."
}
</pre>

## Tasks

### Webpack

Run node script

<pre>
gradle webpack -Pargs="..." // webpack --mode=production &lt;args&gt;
</pre>

### [WebpackWatch](https://plugins.gradle.org/plugin/de.solugo.gradle.webpack)
Run node script

<pre>
gradle webpackWatch -Pargs="..." // webpack --mode=development --watch &lt;args&gt;
</pre>
