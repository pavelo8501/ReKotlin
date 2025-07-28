package po.lognotify.extensions

import po.lognotify.TasksManaged
import po.lognotify.action.ActionSpan
import po.lognotify.anotations.LogOnFault
import po.lognotify.common.containers.ActionContainer
import po.lognotify.common.containers.RunnableContainer
import po.lognotify.exceptions.handleException
import po.lognotify.tasks.TaskBase
import po.misc.containers.withReceiverAndResult
import po.misc.functions.repeater.models.RepeatStats
import po.misc.functions.repeater.repeatOnFault
import po.misc.reflection.classes.ClassRole
import po.misc.reflection.classes.overallInfo
import po.misc.reflection.classes.overallInfoFromType
import po.misc.reflection.properties.takePropertySnapshot
import kotlin.reflect.KType


@PublishedApi
internal fun <T: TasksManaged, R: Any?> onFailure(stats: RepeatStats, container: ActionContainer<T, R>){
    val snapshot = takePropertySnapshot<T, LogOnFault>(container.receiver)
    container.actionSpan.addPropertySnapshot(snapshot)
    handleException(stats.exception, container, null)
}

inline fun <T: TasksManaged, reified R : Any?>  T.runAction(
    actionName: String,
    crossinline block: T.()->R
):R {

    val actionSpan = ActionSpan<T, R>(actionName, this, taskHandler.task)

    taskHandler.task.addActionSpan(actionSpan)
    val actionContainer = ActionContainer.create(actionSpan)
    actionContainer.classInfoProvider.registerProvider { overallInfo<R>(ClassRole.Result) }

    return repeatOnFault({
        setMaxAttempts(0).onException { stats -> onFailure(stats, actionContainer) }
    }){
        actionContainer.withReceiverAndResult(block)
    }
}

inline fun <T: TasksManaged, R: Any?> T.runAction(
    actionName: String,
    resultType: KType,
    crossinline block: T.()->R
):R {
    val newActionSpan = ActionSpan<T, R>(actionName, this, taskHandler.task)
    newActionSpan.setResultType(resultType)
    newActionSpan
    taskHandler.task.addActionSpan(newActionSpan)
    val actionContainer = ActionContainer.create(newActionSpan)
    actionContainer.classInfoProvider.registerProvider { overallInfoFromType(ClassRole.Result, resultType) }

    return repeatOnFault({
        setMaxAttempts(0).onException { stats -> onFailure(stats, actionContainer) }
    }){
        actionContainer.withReceiverAndResult(block)
    }
}
