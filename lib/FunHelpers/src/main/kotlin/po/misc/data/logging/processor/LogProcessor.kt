package po.misc.data.logging.processor

import po.misc.context.component.Component
import po.misc.data.logging.Loggable
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralRecord


class LogProcessor <H: Component, LR: Loggable>(
    override val host: H
): LogProcessorBase<LR>(host) {

    fun <PR: ProceduralRecord, R> logScope(record: PR,  subject: String, block: ProceduralFlow<H, PR>.()-> R):R {
        val flow = ProceduralFlow(host,  subject, record)
        val result = flow.block()
        @Suppress("Unchecked_Cast")
        logData(record as LR)
        return result
    }

    override fun outputOrNot(data: LR) {
        if (data.topic >= verbosity.minTopic) {
            data.echo()
        }
    }
}


