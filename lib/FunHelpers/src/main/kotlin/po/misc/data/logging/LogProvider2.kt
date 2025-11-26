package po.misc.data.logging


//interface LogProvider2<LR: StructuredLoggable>: Component{
//
//    override fun notify(logMessage: LogMessage): StructuredLoggable{
//        logMessage.output()
//        return logMessage
//    }
//
//    override fun info(subject: String, text: String): LogMessage{
//        val message = infoMsg(subject, text)
//        notify(message)
//        return message
//    }
//
//    fun info(subject: InfoSubject, text: String? = null): LogMessage{
//       val message = if(text != null){
//            subject.updateText(text).toLogMessage(this)
//        }else{
//            subject.toLogMessage(this)
//        }
//        notify(message)
//        return message
//    }
//
//    fun warn(subject: String, throwable: Throwable, tracker: LogTracker = LogTracker.Enabled):LogMessage{
//        val warningMessage = warning(subject, throwable.throwableToText(), tracker)
//        notify(warningMessage)
//        return warningMessage
//    }
//
//    override fun warn(subject: String, text: String, tracker: LogTracker): LogMessage{
//        val warningMessage = warning(subject, text,tracker)
//        notify(warningMessage)
//        return warningMessage
//    }
//}

//interface LogEmitterNew<H: Component, LR: StructuredLoggable>: Component{
//
//    val logProcessor: LogProcessor<H, LR>
//
//    override fun info(subject: String, text: String): LogMessage{
//       val message = infoMsg(subject, text)
//       logProcessor.log(message)
//       return message
//    }
//
//    fun info(subject: InfoSubject, text: String): LogMessage{
//        val message = infoMsg(subject, text)
//        logProcessor.log(message)
//        return message
//    }
//
//    override fun warn(subject: String, text: String, tracker: LogTracker): LogMessage{
//        val warning = warning(subject, text, tracker)
//        logProcessor.log(warning)
//        return warning
//    }
//
//    override fun warn(subject: String, throwable: Throwable):LogMessage{
//        val warning =  warning(subject, throwable, LogTracker.Enabled)
//        logProcessor.log(warning)
//        return warning
//    }
//}

//
//inline fun <SL:  StructuredLoggable, H: Component, R>  LogEmitterNew<H, SL>.proceduralScope(
//    record: SL,
//    crossinline block: ProceduralFlow<H>.(ProceduralRecord)-> R
//):R {
//
//    val flow =  logProcessor.createProceduralFlow(record)
//    val result =  block.invoke(flow, flow.proceduralRecord)
//    logProcessor.finalizeFlow(record, flow)
//    return result
//}
//

