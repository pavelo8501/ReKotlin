package po.test.misc.data.logging.procedural

import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.data.logging.Loggable
import po.misc.data.logging.models.Notification
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.logging.processor.logProcessor


abstract class ProceduralTest {

    protected class Component1(
        var subject: String = "Processing"
    ): Component{
        override val componentID: ComponentID = componentID("component_1")
        val processor: LogProcessor<Component1, Notification> = logProcessor()

        override fun notify(loggable: Loggable) {
            processor.logData(loggable.toNotification())
        }

        fun emmitInfo(text: String){
            info(subject, text)
        }

        fun <T: Any> emmitInfoWithResult(text: String, result:T):T{
            info(subject, text)
            return result
        }

    }

    protected class Component2(
        var subject: String = "Processing"
    ): Component{
        override val componentID: ComponentID = componentID("component_2")
        val processor: LogProcessor<Component2, Notification> = logProcessor()

        override fun notify(loggable: Loggable) {
           val notification =  loggable.toNotification()
            processor.logData(notification)
        }

        fun emmitInfo(text: String){
            info(subject, text)
        }

        fun <T> emmitInfoWithResult(text: String, result:T):T{
            info(subject, text)
            return result
        }

    }



}