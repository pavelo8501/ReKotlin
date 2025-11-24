package po.misc.data.logging.processor.parts

import po.misc.data.logging.processor.LogForwarder


abstract  class EmitHub {
    val forwarder: LogForwarder = LogForwarder()

}