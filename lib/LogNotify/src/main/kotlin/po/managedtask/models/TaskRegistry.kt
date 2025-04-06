package po.managedtask.models

import po.managedtask.classes.task.ResultantTask
import po.managedtask.classes.task.RootTask
import po.managedtask.classes.task.TaskSealedBase


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