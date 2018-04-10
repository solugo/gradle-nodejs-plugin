package de.solugo.gradle.nodejs

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
        final NodeJsUtil nodeUtil = NodeJsUtil.getInstance(this.project.nodejs.version)

        final modules = new ArrayList<String>(this.require)

        final String installProperty = project.getProperties().get(PROPERTY_INSTALL)
        if (installProperty != null) {
            modules.addAll(installProperty.split())
        }

        modules.removeAll {nodeUtil.resolveModule(it) != null}

        if (!modules.empty) {
            this.project.exec {
                executable = nodeUtil.resolveCommand("npm")
                args =  ["install"] + modules
                standardOutput = new ByteArrayOutputStream()
                errorOutput = new ByteArrayOutputStream()
            }
        }

        final commandLine = new ArrayList<String>(this.getCommandLine())

        if (commandLine.size() > 0) {
            final exec = nodeUtil.resolveCommand(commandLine.get(0))
            if (exec != null) {
                commandLine.set(0, exec.getAbsolutePath())
            }
        }

        final String argsProperty = project.getProperties().get(PROPERTY_ARGS)
        if (argsProperty != null) {
            commandLine.addAll(argsProperty.split())
        }


        println("Running ${commandLine.join(" ")}")

        this.commandLine(commandLine)

        if (this.environment['Path'] != null) {
            this.environment['Path'] = nodeUtil.bin.absolutePath + File.pathSeparator + this.environment['Path']
        } else {
            this.environment['PATH'] = nodeUtil.bin.absolutePath + File.pathSeparator + this.environment['PATH']
        }

        this.standardInput = System.in
        this.standardOutput = System.out
        this.errorOutput = System.err

        super.exec()
    }

}
