package po.lognotify.models

import po.lognotify.classes.task.ResultantTask
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.TaskSealedBase

class TaskRegistry<R>(
    val hierarchyRoot: RootTask<R>
) {
    val childTasks: MutableList<TaskSealedBase<*>> = mutableListOf<TaskSealedBase<*>>()

    fun registerChild(task: TaskSealedBase<*>) {
        childTasks.add(task)
    }

    fun getLastRegistered(): TaskSealedBase<*>{
        return if(childTasks.count() == 0){
            hierarchyRoot
        }else{
            childTasks.last()
        }
    }

    fun getAsResultantTaskList (): List<ResultantTask>{
        val list = mutableListOf<ResultantTask>()
        list.add(hierarchyRoot)
        list.addAll(childTasks)
        return  list
    }
}