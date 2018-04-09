[![Travis](https://img.shields.io/travis/solugo/node-gradle-plugin.svg?style=for-the-badge)](https://travis-ci.org/solugo/node-gradle-plugin)
[![License](https://img.shields.io/github/license/solugo/node-gradle-plugin.svg?style=for-the-badge)](https://github.com/solugo/node-gradle-plugin/blob/master/LICENSE)
[![Maven metadata URI](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/gradle/plugin/de/solugo/gradle/node-gradle-plugin/maven-metadata.xml.svg?style=for-the-badge)](https://plugins.gradle.org/plugin/de.solugo.gradle.node)

# Gradle Node Plugin
This plugin allows to use node script and tools via gradle.

## Task rules

### Node
##### Syntax
<pre>
gradle node<alias> -Pargs="..."
</pre>

##### Example
<pre>
gradle node -Pargs="--version"
</pre>

### Npm
##### Syntax
<pre>
gradle npm<task> -Pargs="..."
</pre>

##### Example
<pre>
gradle npm -Pargs="--version"
</pre>

### Npx
##### Syntax
<pre>
gradle npx<module> -Pargs="..."
</pre>

##### Example
<pre>
gradle npxWebpack -Pargs="--version"
</pre>


## Examples

### Using Npm
##### Initialize default package.json
<pre>
gradle npmInit -Pargs="-f"
</pre>

##### Install dependency
<pre>
gradle npmInstall -Pargs="--save-dev webpack-cli"
</pre>


### Using webpack

##### Setup
<pre>
gradle --console=plain npmInit
gradle npmInstall -Pargs="webpack webpack-cli --save-dev"
</pre>

##### Modify package.json
<pre>
"scripts": {
  "compile": "webpack-cli --mode=production"
}
</pre>

##### Build
<pre>
gradle npmRunCompile
</pre>

