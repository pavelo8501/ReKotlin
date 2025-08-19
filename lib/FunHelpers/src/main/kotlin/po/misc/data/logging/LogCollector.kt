package po.misc.data.logging

import po.misc.data.printable.PrintableBase

interface LogCollector {

    fun provideData(record: PrintableBase<*>)

}