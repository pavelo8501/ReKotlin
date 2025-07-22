package po.misc.functions.dsl


interface DSLStorage2<T, R> where T: Any, R:Any{
    val dslBlocks: List<DSLBlock<T, R>>
}

class DSLContainer2<T, R>(
    private var constructLambda : (DSLBlock<T, R>.() -> Unit)? = null
): DSLStorage2<T,R> where T: Any, R:Any {

    private var rootContainer: DSLBlock<T, R>? = null

    init {
        constructLambda?.let {
            build(it)
        }
    }
    private val dslBlocksBacking: MutableList<DSLBlock<T, R>> = mutableListOf()
    override val dslBlocks: List<DSLBlock<T, R>> get() = dslBlocksBacking

    val dslBlocksCount: Int get() = dslBlocks.size
    val dslBlocksTotalSize: Int get() = dslBlocksBacking.sumOf { it.subContainersSize }

    private fun blockConstructor(): DSLBlock<T,R>{
        val newBlock =  DSLBlock(this)
        dslBlocksBacking.add(newBlock)
       return newBlock
    }
    fun saveDSLBlock(dslBlock:DSLBlock<T, R>):DSLBlock<T, R>{
        dslBlocksBacking.add(dslBlock)
        return dslBlock
    }


    fun build(lambda: DSLBlock<T, R>.() -> Unit): DSLBlock<T, R> {
        val newBlock = blockConstructor()
        newBlock.lambda()
        return newBlock
    }

}