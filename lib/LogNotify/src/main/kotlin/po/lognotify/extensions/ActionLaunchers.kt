package po.lognotify.extensions

import po.lognotify.TasksManaged
import po.lognotify.action.ActionSpan
import po.lognotify.common.containers.ActionContainer
import po.lognotify.common.containers.RunnableContainer
import po.lognotify.execution.controlledRun
import po.misc.reflection.classes.ClassRole
import po.misc.reflection.classes.overallInfo
import po.misc.reflection.classes.overallInfoFromType
import kotlin.reflect.KType

inline fun <T: TasksManaged, reified R : Any?>  T.runInlineAction(
    actionName: String,
    crossinline block: RunnableContainer<T, R>.()->R
):R {
    val taskHandler = taskHandler()

    val newActionSpan = ActionSpan<T, R>(actionName, this, taskHandler.task)

    taskHandler.task.addActionSpan(newActionSpan)
    val container =  ActionContainer.create(newActionSpan)
    container.classInfoProvider.registerProvider { overallInfo<R>(ClassRole.Result) }
    return container.controlledRun {
        block.invoke(container as RunnableContainer<T, R>)
    }
}

fun <T:TasksManaged, R: Any?> T.runAction(
    actionName: String,
    resultType: KType,
    block: RunnableContainer<T, R>.()->R
):R {

    val taskHandler = taskHandler()

    val newActionSpan = ActionSpan<T, R>(actionName, this, taskHandler.task)
    newActionSpan.resultType = resultType
    taskHandler.task.addActionSpan(newActionSpan)
    val container = ActionContainer.create(newActionSpan)
    container.classInfoProvider.registerProvider { overallInfoFromType(ClassRole.Result, resultType) }

    return try {
        block.invoke(container as RunnableContainer<T, R>)
    }catch (th: Throwable){
       throw th
    }

//    return container.controlledRun{
//        block.invoke(container as RunnableContainer<T, R>)
//    }
}

inline fun <T:TasksManaged, reified R: Any?>  T.action(
    actionName: String,
    crossinline block: RunnableContainer<T, R>.()->R
):R = runInlineAction(actionName, block)