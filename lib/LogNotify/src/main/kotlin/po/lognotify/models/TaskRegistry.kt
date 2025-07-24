package po.lognotify.models


import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.Task
import po.lognotify.tasks.TaskBase
import po.lognotify.tasks.interfaces.ResultantTask
import po.misc.context.CTX

class TaskRegistry<T: CTX, R>(
    val dispatcher: TaskDispatcher,
    val hierarchyRoot: RootTask<T, R>
) {
    val tasks: MutableMap<TaskKey, Task<*, *>> = mutableMapOf()

    val totalCount: Int get() = tasks.size + 1
    val childCount: Int get() = tasks.size

    fun registerChild(task: Task<*, *>) {
        tasks[task.key] = task
        dispatcher.notifyUpdate(TaskDispatcher.UpdateType.OnTaskCreated, task)
    }

    fun getLastSubTask(): Task<*, *>?{
        return tasks.values.lastOrNull()
    }

    fun setChildTasksStatus(status: ExecutionStatus, taskCalling: TaskBase<*,*>){
        val subTasks = getSubTasks(taskCalling)
        subTasks.forEach {
            it.changeStatus(status)
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


    fun getActiveTask(): TaskBase<*, *>{
       val found =  tasks.values.firstOrNull { it.executionStatus == ExecutionStatus.Active }
       if(found == null){ return  hierarchyRoot }
       return found
    }
}