package po.lognotify.common.configuration

import po.lognotify.TasksManaged

fun TasksManaged.newTaskConfig(): TaskConfig = TaskConfig()

fun TasksManaged.applyConfig(configBuilder: TaskConfig.() -> Unit) {
    taskHandler.task.config.configBuilder()
    taskHandler.task.registry.tasks.values
        .forEach { it.config.configBuilder() }
}
