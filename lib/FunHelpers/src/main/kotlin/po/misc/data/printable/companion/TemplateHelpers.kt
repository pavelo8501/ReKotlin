package po.misc.data.printable.companion

import po.misc.data.printable.PrintableBase
import po.misc.functions.dsl.ContainingDSLBlock
import po.misc.functions.dsl.DSLConstructor
import po.misc.functions.dsl.handlers.DSLHandler
import po.misc.functions.dsl.helpers.nextBlock


fun <T: PrintableBase<T>>  DSLConstructor<T, String>.nextLine(
    block: T.(DSLHandler<T, String>)-> String
): ContainingDSLBlock<T, String, T> = nextBlock(block)




fun <T: PrintableBase<T>>  DSLConstructor<T, String>.appendLine(
    block: T.(DSLHandler<T, String>)-> String
):ContainingDSLBlock<T, String, T>{
    return  nextBlock(block)
}