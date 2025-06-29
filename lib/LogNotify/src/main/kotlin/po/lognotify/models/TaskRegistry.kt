package po.lognotify.models


import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.Task
import po.lognotify.classes.task.TaskBase
import po.lognotify.classes.task.interfaces.ResultantTask

class TaskRegistry<T, R>(
    val dispatcher: TaskDispatcher,
    val hierarchyRoot: RootTask<T, R>
) {
    val tasks: MutableMap<TaskKey, Task<*, *>> = mutableMapOf()
    fun registerChild(task: Task<*, *>) {
        tasks[task.key] = task
        dispatcher.notifyUpdate(TaskDispatcher.UpdateType.OnTaskCreated, task)
    }

    fun getLastSubTask(): Task<*, *>?{
        return tasks.values.lastOrNull()
    }

    fun setChildTasksStatus(status: TaskBase.TaskStatus, taskCalling: TaskBase<*,*>){
        val subTasks = getSubTasks(taskCalling)
        subTasks.forEach {
            it.taskStatus = status
        }
    }

    fun getAsResultantTaskList (): List<ResultantTask<*, *>>{
        val list = mutableListOf<ResultantTask<*, *>>()
        list.add(hierarchyRoot)
        list.addAll(tasks.values)
        return  list
    }

    fun getFirstSubTask(task: TaskBase<*, *>): Task<*,*>?{
       return when(task){
            is RootTask->{
                val selectedTask = tasks.values.firstOrNull()
                selectedTask
            }
            is Task<*,*>->{
              val selectedTask =  tasks
                    .filterKeys { it.taskId > task.key.taskId }
                    .minByOrNull { it.key.taskId }
                    ?.value
                selectedTask
            }
        }
    }

    fun getSubTasks(task: TaskBase<*, *>): List<Task<*,*>>{
      return  when(task){
            is RootTask->{
                tasks.values.toList()
            }
            is Task<*,*>->{
                tasks.filterKeys { it.taskId > task.key.taskId }
                    .toSortedMap(compareBy { it.taskId })
                    .values
                    .toList()
            }
        }
    }

    fun taskCount(): Int {
       return tasks.size + 1
    }
}