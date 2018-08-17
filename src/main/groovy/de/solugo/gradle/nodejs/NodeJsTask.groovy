package de.solugo.gradle.nodejs

import org.apache.tools.ant.taskdefs.condition.Os
import org.apache.tools.ant.types.Environment
import org.gradle.api.tasks.AbstractExecTask

import javax.inject.Inject

class NodeJsTask<T extends NodeJsTask<T>> extends AbstractExecTask<T> {

    public static final String PROPERTY_ARGS = "args"
    public static final String PROPERTY_INSTALL = "install"

    def require = new ArrayList<String>()

    @Inject
    NodeJsTask() {
        this(NodeJsTask)
    }

    NodeJsTask(final Class<T> taskType) {
        super(taskType)
    }

    @Override
    protected void exec() {
        final NodeJsUtil nodeJsUtil = NodeJsUtil.getInstance(this.project.nodejs.version)

        final modules = new ArrayList<String>(this.require)

        final String installProperty = project.getProperties().get(PROPERTY_INSTALL)

        if (installProperty != null) {
            modules.addAll(installProperty.split())
        }

        modules.removeAll { this.resolveModule(nodeJsUtil, it) != null }

        if (!modules.empty) {
            this.project.exec {
                modifyEnvironment(nodeJsUtil, environment)
                workingDir = project.nodejs.rootPath
                executable = resolveCommand(nodeJsUtil, "npm")
                args = ["install"] + modules
                standardOutput = new ByteArrayOutputStream()
                errorOutput = new ByteArrayOutputStream()
            }
        }

        final commandLine = new ArrayList<String>(this.getCommandLine())

        if (commandLine.size() > 0) {
            final exec = this.resolveCommand(nodeJsUtil, commandLine.get(0))
            if (exec != null) {
                commandLine.set(0, exec.getAbsolutePath())
            }
        }

        final String argsProperty = project.getProperties().get(PROPERTY_ARGS)
        if (argsProperty != null) {
            commandLine.addAll(argsProperty.split())
        }

        try {
            modifyEnvironment(nodeJsUtil, this.environment)
            this.workingDir = project.nodejs.rootPath
            this.commandLine(commandLine)
            this.standardInput = System.in
            this.standardOutput = System.out
            this.errorOutput = System.err

            super.exec()
        } catch (Exception ex) {
            throw new RuntimeException("Error running ${commandLine.join(" ")}", ex)
        }
    }


    protected File resolveCommand(final NodeJsUtil nodeJsUtil, final String command) {
        final String name
        if (Os.isFamily(Os.FAMILY_WINDOWS)){
            name = "${command}.cmd"
        } else {
            name = command
        }

        final File absoluteFile = new File(name);
        if (absoluteFile.exists()) {
            return absoluteFile
        }

        final File globalFile = new File(nodeJsUtil.bin, name)
        if (globalFile.exists()) {
            return globalFile
        }

        final File localFile = new File(this.joinPaths(this.project.nodejs.rootPath, "node_modules", ".bin", name))
        if (localFile.exists()) {
            return localFile
        }

        return null
    }

    protected File resolveModule(final NodeJsUtil nodeJsUtil, final String module) {
        final File globalFile = new File(this.joinPaths(nodeJsUtil.modules.absolutePath, module))
        if (globalFile.exists()) {
            return globalFile
        }

        final File localFile = new File(this.joinPaths(this.project.nodejs.rootPath, "node_modules", module))
        if (localFile.exists()) {
            return localFile
        }

        return null
    }

    protected void modifyEnvironment(final NodeJsUtil nodeJsUtil, final Map<String, Object> environment) {
        final StringBuilder builder = new StringBuilder()
        builder.append(this.joinPaths(this.project.nodejs.rootPath, "node_modules", ".bin"))
        builder.append(File.pathSeparator)
        builder.append(nodeJsUtil.bin.absolutePath)
        builder.append(File.pathSeparator)

        println("Environment: " + builder.toString())

        if (environment['Path'] != null) {
            builder.append(environment['Path'])
            environment['Path'] = builder.toString()
        } else {
            builder.append(environment['PATH'])
            environment['PATH'] = builder.toString()
        }
    }

    protected String joinPaths(final String... parts) {
        final StringBuilder builder = new StringBuilder();
        for (final String part : parts) {
            if (part != null && !part.isEmpty()) {
                if (builder.length() > 0) {
                    builder.append(File.separator)
                }
                builder.append(part)
            }
        }
        return builder.toString();
    }

}
