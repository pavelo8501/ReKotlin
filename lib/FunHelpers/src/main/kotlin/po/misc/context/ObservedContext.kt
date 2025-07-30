package po.misc.context

import po.misc.exceptions.ManagedException


interface ObservedContext: CTX{

    val exceptionOutput: ((ManagedException)-> Unit)?
}