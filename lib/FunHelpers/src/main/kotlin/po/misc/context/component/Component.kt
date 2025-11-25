package po.misc.context.component

import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.factory.toLogMessage
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.log_subject.DebugSubject
import po.misc.data.logging.log_subject.InfoSubject
import po.misc.data.logging.log_subject.LogSubject
import po.misc.data.logging.parts.LogTracker
import po.misc.exceptions.throwableToText


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

    val componentID: ComponentID get() = ComponentID(this)

    val componentName: String get(){
       val componentIdentification : ComponentID?  = componentID as ComponentID?
       return componentIdentification?.componentName ?: "Component"
    }

    fun notify(logMessage: LogMessage): StructuredLoggable{
        logMessage.output()
        return logMessage
    }

    fun info(subject: String, text: String): LogMessage{
        val infoMessage =  infoMsg(subject, text)
        notify(infoMessage)
        return infoMessage
    }
    fun info(subject: LogSubject): LogMessage {
        val infoMessage = subject.toLogMessage(this)
        notify(infoMessage)
        return infoMessage
    }

    /**
     * Emits a debug message. Useful for internal tracing.
     */
    fun debug(subject: String, text: String, outputImmediately: Boolean = false): LogMessage {
        val message =  debugMsg(subject, text)
        if(outputImmediately){
            val message =  debugMsg(subject, text)
            message.output()
        }else{
            notify(message)
        }
        return message
    }

    fun warn(subject: String, text: String, tracker: LogTracker = LogTracker.Enabled): LogMessage {
        val warningMessage = warning(subject, text,tracker)
        notify(warningMessage)
        return warningMessage
    }

    fun warn(subject: String, throwable: Throwable): LogMessage = warn(subject, throwable.throwableToText(),  LogTracker.Enabled)
    fun warn(subject: String, text: String): LogMessage = warn(subject, text,  LogTracker.Enabled)
    fun warn(subject: LogSubject, text: String): LogMessage =  warn(subject.subjectName, text, LogTracker.Enabled)

    override fun notify(loggable: Loggable): StructuredLoggable = notify(loggable.toLogMessage())

    fun message(
        subject: String,
        text: String,
        topic: NotificationTopic = NotificationTopic.Info,
        tracker: LogTracker = LogTracker.Enabled
    ): LogMessage {
        return  LogMessage(this, subject, text, topic, tracker)
    }

    fun debugMsg(
        logSubject: DebugSubject,
    ): LogMessage {
        return message(logSubject.subjectName, logSubject.subjectText, NotificationTopic.Debug, LogTracker.Enabled)
    }

    fun debugMsg(
        subject: String,
        text: String,
    ): LogMessage {
        return message(subject, text, NotificationTopic.Debug, LogTracker.Enabled)
    }

    fun infoMsg(
        subject: InfoSubject,
        text: String? = null
    ): LogMessage{
        return message( subject.subjectName, text?:subject.subjectText, NotificationTopic.Info)
    }

    fun infoMsg(
        subject: String,
        text: String
    ): LogMessage = message(subject, text, NotificationTopic.Info)

    fun warning(
        subject: String,
        text: String,
        tracker: LogTracker = LogTracker.Enabled
    ): LogMessage{
        return message(subject, text, NotificationTopic.Warning, tracker)
    }

    fun warning(
        subject: String,
        throwable: Throwable,
        tracker: LogTracker = LogTracker.Enabled
    ): LogMessage{
       return message(subject, throwable.throwableToText(), NotificationTopic.Warning, tracker)
    }

    companion object {



    }
}

