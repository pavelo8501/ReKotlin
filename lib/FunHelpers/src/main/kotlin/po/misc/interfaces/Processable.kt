package po.misc.interfaces

import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import kotlin.coroutines.CoroutineContext

interface Processable:  CoroutineContext.Element, CTX {
    fun provideData(record: PrintableBase<*>)
}