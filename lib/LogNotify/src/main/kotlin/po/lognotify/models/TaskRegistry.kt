package po.lognotify.models


import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.RootTaskSync
import po.lognotify.classes.task.SubTaskSync
import po.lognotify.classes.task.TaskAsyncBase
import po.lognotify.classes.task.TaskBaseSync
import po.lognotify.classes.task.interfaces.ResultantTask


interface CommonTaskRegistry{

    fun getCount(): Int
    fun getAsResultantTaskList (): List<ResultantTask>

    fun getLastRegistered(): ResultantTask
}


class TaskRegistrySync<R>(
    val hierarchyRoot: RootTaskSync<R>
):CommonTaskRegistry {

    val childTasks: MutableList<SubTaskSync<*>> = mutableListOf()

    fun registerChild(task: SubTaskSync<*>):SubTaskSync<*> {
        childTasks.add(task)
        return task
    }

    override fun getLastRegistered(): TaskBaseSync<*>{
        return if(childTasks.count() == 0){
            hierarchyRoot
        }else{
            childTasks.last()
        }
    }

    override fun getAsResultantTaskList (): List<ResultantTask>{
        val list = mutableListOf<ResultantTask>()
        list.add(hierarchyRoot)
        list.addAll(childTasks)
        return  list
    }

    override fun getCount(): Int {
        return childTasks.count()
    }
}


class TaskRegistry<R>(
    val hierarchyRoot: RootTask<R>
):CommonTaskRegistry {
    val childTasks: MutableList<TaskAsyncBase<*>> = mutableListOf<TaskAsyncBase<*>>()

    fun registerChild(task: TaskAsyncBase<*>) {
        childTasks.add(task)
    }

    override fun getLastRegistered(): TaskAsyncBase<*>{
        return if(childTasks.count() == 0){
            hierarchyRoot
        }else{
            childTasks.last()
        }
    }

    override fun getAsResultantTaskList (): List<ResultantTask>{
        val list = mutableListOf<ResultantTask>()
        list.add(hierarchyRoot)
        list.addAll(childTasks)
        return  list
    }

    override fun getCount(): Int {
       return childTasks.count()
    }
}