package po.lognotify.models


import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.Task
import po.lognotify.classes.task.interfaces.ResultantTask
import po.misc.types.UpdateType

class TaskRegistry<R>(
    val dispatcher: TaskDispatcher,
    val hierarchyRoot: RootTask<R>
) {
    val tasks: MutableList<Task<*>> = mutableListOf<Task<*>>()

    fun registerChild(task: Task<*>) {
        tasks.add(task)
        dispatcher.notifyUpdate(UpdateType.OnCreated, task)
    }

    fun getLastChild(): Task<*>?{
        return tasks.lastOrNull()
    }

    fun getAsResultantTaskList (): List<ResultantTask<*>>{
        val list = mutableListOf<ResultantTask<*>>()
        list.add(hierarchyRoot)
        list.addAll(tasks)
        return  list
    }

    fun taskCount(): Int {
       return tasks.count()
    }
}