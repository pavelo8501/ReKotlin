package po.misc.functions.dsl.helpers

import po.misc.functions.dsl.ConstructableDSL
import po.misc.functions.dsl.ContainingDSLBlock
import po.misc.functions.dsl.DSLConstructor
import po.misc.functions.dsl.handlers.DSLHandler

fun <T: Any, R: Any> ConstructableDSL<T, R>.dslConstructor():DSLConstructor<T,R>{
    return DSLConstructor()
}


fun <T: Any, R: Any> DSLConstructor<T, R>.nextBlock(
    block:T.(DSLHandler<T, R>)->R
): ContainingDSLBlock<T, R, T> = addBlock(block)




fun <T: Any, R: Any, PT: Any> DSLConstructor<PT, R>.nextBlock(
    adapter: (PT)->T,
    block:T.()->R
): ContainingDSLBlock<T, R, PT> = addSubBlock(adapter, block)



fun <T: Any, R: Any, PT: Any> DSLConstructor<PT, R>.nextBlockWithHandler(
    handler: DSLHandler<T, R>,
    adapter: (PT?)->T,
    block:T.(DSLHandler<T, R>)->R
): ContainingDSLBlock<T, R, PT> = addSubBlockWithHandler(handler, adapter, block)

