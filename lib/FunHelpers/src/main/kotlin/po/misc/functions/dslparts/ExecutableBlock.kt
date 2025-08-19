package po.misc.functions.dslparts


interface ExecutableBlock<I : Any, O : Any> {

    fun execute(input: I): O

    infix fun <NextOut : Any> then(next: ExecutableBlock<O, NextOut>): ExecutableBlock<I, NextOut> {
        return object : ExecutableBlock<I, NextOut> {
            override fun execute(input: I): NextOut {
                val intermediate = this@ExecutableBlock.execute(input)
                return next.execute(intermediate)
            }
        }
    }
}

fun <I : Any, O : Any> I.pipe(block: ExecutableBlock<I, O>): O = block.execute(this)