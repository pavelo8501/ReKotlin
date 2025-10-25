package po.misc.context.component

import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.output
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.models.Notification
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.debugging.ClassResolver
import po.misc.exceptions.ManagedException
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.types.helpers.simpleOrAnon


/**
 * Defines a runtime component that participates in structured logging and traceable execution.
 *
 * A `Component` extends [TraceableContext] and provides a stable identity, human-readable name,
 * and configurable verbosity level via [componentID].
 *
 * ### Key Responsibilities
 * - Supplies a unique [componentID] used across logging, error reporting and tracing.
 * - Provides semantic component naming via [componentName].
 * - Exposes current logging [verbosity] level inherited from its identifier.
 * - Supplies message formatting helpers ([infoMessageFormatter], [warnMessageFormatter])
 *   for consistent visual output in console or file logs.
 *
 * ### Identity and Verbosity
 * By default, [componentID] is lazily computed from the implementing class name and its reflection-based
 * [ClassInfo]. This identity can be customized or mutated when a component represents a dynamic or
 * parameterized runtime entity.
 *
 * ### Notification Endpoint
 * A component may emit log or notification data via [notify], which delegates to the underlying
 * logging system. Implementers are not required to override this behavior unless interception
 * or transformation of log messages is necessary.
 *
 * @see ComponentID for identity structure
 * @see TraceableContext to distinguish real component context from plain `Any`
 */
interface Component : TraceableContext {

    val componentID: ComponentID get() = ComponentID(this::class.simpleOrAnon, ClassResolver.classInfo(this))

    override fun notify(loggable: Loggable){
        if(loggable.topic >= componentID.verbosity.minTopic){
            loggable.output()
        }
    }

    fun notification(topic: NotificationTopic, subject: String, text: String): Notification{
       return Notification(this, topic, subject, text)
    }

    fun Loggable.toNotification(): Notification{
       return Notification(this)
    }

    fun Loggable.toProcedural(): ProceduralRecord{
       return ProceduralRecord(this)
    }

}


fun Component.managedException(message: String): ManagedException{
    val exception =  ManagedException(this, message)
    exception.extractTrace(this)
    return exception
}

