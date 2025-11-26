package po.test.misc.callback.signal

import org.junit.jupiter.api.Test
import po.misc.callbacks.signal.SignalOptions
import po.misc.callbacks.signal.listen
import po.misc.callbacks.signal.signal
import po.misc.callbacks.signal.signalOf
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.data.logging.Verbosity
import po.misc.functions.NoResult
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestSignalBuilder : Component {

    internal val logArrival  = signalOf<String>(NoResult, SignalOptions("logArrival"))
    internal val logWithResult  = signal<String, Int>{
        signalName("logWithResult").verbosity = Verbosity.Debug
    }

    @Test
    fun `Name injection injection`(){
        val notify = signal<String, Unit>{
            signalName("notify")
        }
        assertEquals("notify", notify.componentID.componentName)
        assertNotNull(notify.componentID.classInfo)
        notify.componentID.output()
    }

    @Test
    fun `Subscribing signal updates by extension`(){
        var logRecord: String? = null
        listen(logArrival){
            logRecord = it
        }
        logArrival.trigger("Some value")
        assertEquals("Some value", logRecord)
    }

    @Test
    fun `Subscribing signal updates by extension while producing result`(){
        var logRecord: String? = null
        val strValue = "Some value"
        listen(logWithResult){
            logRecord = it
            it.count()
        }
        val result =  logWithResult.trigger(this, strValue)
        assertEquals(strValue, logRecord)
        assertEquals(strValue.count(), result)
    }
}