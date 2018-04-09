package de.solugo.gradle.node

import org.gradle.api.tasks.AbstractExecTask

import javax.inject.Inject
import javax.naming.OperationNotSupportedException

class NodeTask<T extends NodeTask<T>> extends AbstractExecTask<T> {

    public static final String PROPERTY_ARGS = "args"

    @Inject
    NodeTask() {
        this(NodeTask)
    }

    NodeTask(final Class<T> taskType) {
        super(taskType)
        this.standardInput = System.in
        this.standardOutput = System.out
        this.errorOutput = System.err
    }

    @Override
    protected void exec() {
        final NodeUtil nodeUtil = NodeUtil.getInstance(this.project.node.version)

        final List<Object> arguments = new ArrayList<>();
        arguments.add(nodeUtil.executable.absolutePath)
        if (this.executable != null) {
            arguments.add(this.executable)
        }
        if (this.args != null && !this.args.empty) {
            arguments.addAll(this.args)
        }
        if (arguments.size() > 1) {
            final target = arguments.get(1).toString()
            final alias = project.node.aliases.get(target)
            if (alias != null) {
                arguments.removeAt(1)
                arguments.addAll(1, alias.split())
            }
        }
        if (arguments.size() > 1) {
            arguments.set(1, this.resolveScript(
                    arguments.get(1).toString(),
                    Arrays.asList(new File("node_modules"), nodeUtil.modules))
            )
        }
        final String property = project.getProperties().get(PROPERTY_ARGS)
        if (property != null) {
            arguments.addAll(property.split())
        }

        println("Running ${arguments.join(" ")}")

        this.commandLine(arguments)

        if (this.environment['Path'] != null) {
            this.environment['Path'] = nodeUtil.executable.parentFile.absolutePath + File.pathSeparator + this.environment['Path']
        } else {
            this.environment['PATH'] = nodeUtil.executable.parentFile.absolutePath + File.pathSeparator + this.environment['PATH']
        }


        super.exec()
    }

    protected String resolveScript(final String script, final List<File> paths) {
        final File absoluteFile = new File(script)

        if (absoluteFile.exists()) {
            return absoluteFile.absolutePath
        }
        for (final File path : paths) {
            final File file = new File(path, script)
            if (file.exists()) {
                return file.absolutePath
            }
        }

        throw new OperationNotSupportedException("Could not find script '${script}' in ${paths}")
    }
}
