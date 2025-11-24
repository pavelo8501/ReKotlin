package po.test.misc.context.log_provider

import org.junit.jupiter.api.Test
import po.misc.context.log_provider.LogProvider
import po.misc.context.log_provider.proceduralScope
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.logging.processor.createLogProcessor
import kotlin.test.assertEquals


class TestLogProviderExtensions : LogProvider{

    override val logProcessor: LogProcessor<TestLogProviderExtensions, LogMessage> = createLogProcessor()


    @Test
    fun `Procedural scope extension successfully starts log flow and returns result`(){

        val initialRecord = infoMsg("Initial", "Start")
        val value = "Result of flow"
        val result = proceduralScope(initialRecord){

            step("Step1"){
                value
            }
        }
        assertEquals(value, result)
    }

}