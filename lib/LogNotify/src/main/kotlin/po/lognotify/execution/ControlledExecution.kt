package po.lognotify.execution

import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.common.containers.ActionContainer
import po.lognotify.common.containers.RunnableContainer
import po.lognotify.common.containers.TaskContainer
import po.lognotify.common.result.PreliminaryResult
import po.lognotify.exceptions.handleException
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.data.styles.SpecialChars
import po.misc.exceptions.ManagedException
import po.misc.reflection.properties.takePropertySnapshot

/**
 * Internal marker interface for tightly controlled execution units within the logging and task management system.
 *
 * Used to represent operations (e.g., tasks, jobs, subprocesses) that are executed within
 * the internal framework's lifecycle-aware, structured context. These operations are typically
 * launched or monitored by internal APIs and benefit from built-in logging, exception handling,
 * and tracking features.
 *
 * This interface should not be exposed outside the framework or used by third-party code.
 * For public extensions or controlled external integrations, see [ExternalExecution].
 *
 * Note: Intentionally `internal` to preserve internal guarantees and evolution flexibility.
 */

@PublishedApi
internal interface ControlledExecution : TasksManaged {

    fun <T : TasksManaged, R : Any?>  RunnableContainer<T, R>.finalizeRun(result:R):R{
        when(this){
            is TaskContainer<*, *> ->{
                sourceTask.complete()
                onResultResolved(result)
            }

            is ActionContainer<*, *> -> {
                actionSpan.complete()
                onResultResolved(result)
            }
        }
        return result
    }

    fun <T : TasksManaged, R : Any?>  RunnableContainer<T, R>.printHierarchy(){
       val text = effectiveActionSpan?.task.toString() +
        effectiveTask.actionSpans.joinToString(separator = SpecialChars.NewLine.char) {
            it.toString()
        }
        print(text)
    }

    fun <T : TasksManaged, R : Any?>  RunnableContainer<T, R>.processExceptionCase(
        container: RunnableContainer<T, R>,
        throwable: Throwable
    ): ManagedException {
        container.notifySourceIsFailing()
        val snapshot = takePropertySnapshot<T, LogOnFault>(container.receiver)
        val managed = handleException(throwable, container, snapshot)
        return managed
    }

    fun <T : TasksManaged, R : Any?> RunnableContainer<T, R>.handleCompletionOrThrow(
        container: RunnableContainer<T, R>,
        managed: ManagedException
    ): PreliminaryResult<R> {
        val classInfo = container.classInfoProvider.resolve()

        val result = PreliminaryResult(classInfo)
       return if(result.resultAcceptsNulls){
            result.provideAcceptableResult(null)
        }else{
            result
        }
    }
}