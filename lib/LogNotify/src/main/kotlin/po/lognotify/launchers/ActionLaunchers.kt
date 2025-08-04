package po.lognotify.launchers

import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.common.containers.ActionContainer
import po.lognotify.exceptions.ExceptionBehaviour
import po.lognotify.exceptions.handleException
import po.misc.containers.withReceiverAndResult
import po.misc.functions.repeater.models.RepeatStats
import po.misc.functions.repeater.repeatOnFault
import po.misc.reflection.classes.ClassRole
import po.misc.reflection.classes.overallInfo
import po.misc.reflection.classes.overallInfoFromType
import po.misc.reflection.properties.takePropertySnapshot
import kotlin.reflect.KType

@PublishedApi
internal fun <T : TasksManaged, R : Any?> onFailure(
    stats: RepeatStats,
    container: ActionContainer<T, R>,
) {
    val snapshot = takePropertySnapshot<T, LogOnFault>(container.receiver)
    container.actionSpan.addPropertySnapshot(snapshot)
    val manged = handleException(stats.exception, container, null)

    ExceptionBehaviour.onExceptionBehaviour(manged, container.source)
}

inline fun <T : TasksManaged, reified R> T.runAction(
    actionName: String,
    crossinline block: T.() -> R,
): R {
    val actionContainer = ActionContainer.create(taskHandler.task.createActionSpan<T, R>(actionName, this))
    actionContainer.classInfoProvider.registerProvider { overallInfo<R>(ClassRole.Result) }

    return repeatOnFault({
        setMaxAttempts(0).onException { stats ->
            onFailure(stats, actionContainer)
        }
    }) {
        actionContainer.withReceiverAndResult(block)
    }
}

inline fun <T : TasksManaged, R> T.runAction(
    actionName: String,
    resultType: KType,
    crossinline block: T.() -> R,
): R {
    val actionContainer = ActionContainer.create(taskHandler.task.createActionSpan<T, R>(actionName, this))
    actionContainer.classInfoProvider.registerProvider { overallInfoFromType(ClassRole.Result, resultType) }
    return repeatOnFault({
        setMaxAttempts(0).onException { stats ->
            onFailure(stats, actionContainer)
        }
    }) {
        actionContainer.withReceiverAndResult(block)
    }
}
