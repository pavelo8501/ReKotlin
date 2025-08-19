package po.misc.functions.dsl

import po.misc.functions.dsl.handlers.DSLHandler


open class DSLConstructor<T: Any,  R: Any>(
    private val constructLambda: (DSLConstructor<T,R>.()-> Unit)? = null
){

    val dslHandler : DSLHandler<T, R> = DSLHandler(this)

    private var defaultActionFn : ((R)->R)? = null

    private var subBlocksBacking: MutableList<ContainingDSLBlock<*, R, T>> = mutableListOf()
    val subBlocks: List<ContainingDSLBlock<*, R, T>> get() = subBlocksBacking

    private var dslBlocksBacking: MutableList<ContainingDSLBlock<T, R, T>> = mutableListOf()
    val  dslDSLBlocks: List<ContainingDSLBlock<T, R, T>> = dslBlocksBacking

    val  blocksTotalCount : Int get() = dslBlocksBacking.sumOf { it.blocksCount } + subBlocks.sumOf { it.blocksCount }

    fun <T2: Any> addSubBlock(adapter:(T)->T2, block:T2.()->R):ContainingDSLBlock<T2, R, T> {
        val subDSLBlock = ContainingDSLBlock(block, adapter)
        subBlocksBacking.add(subDSLBlock)
        return subDSLBlock
    }


    fun <T2: Any> addSubBlockWithHandler(handler:DSLHandler<T2,R>,  adapter:(T?)->T2, block:T2.(DSLHandler<T2, R>)->R):ContainingDSLBlock<T2, R, T> {
        val subDSLBlock = ContainingDSLBlock(block, handler,  adapter)
        subBlocksBacking.add(subDSLBlock)
        return subDSLBlock
    }


    fun addBlock(block:T.(DSLHandler<T, R>)->R): ContainingDSLBlock<T, R, T> {
        val subDSLBlock = ContainingDSLBlock<T, R, T>(block, dslHandler, null)
        dslBlocksBacking.add(subDSLBlock)
        return subDSLBlock
    }

    fun build(): DSLConstructor<T, R> {
        this.constructLambda?.invoke(this)
        return this
    }

    fun build(function: DSLConstructor<T,R>.()-> Unit): DSLConstructor<T, R>{
        function.invoke(this)
        return this
    }

    fun resolve(inputData: T): List<R>{
        val selfResults = dslBlocksBacking.flatMap { it.resolve(inputData) }
        val childResults = subBlocksBacking.flatMap { it.resolveWithParent(inputData) }
        return selfResults + childResults
    }

    fun resolve(inputData: T, adapterFn: (List<R>) -> R):R{
        val listResult = resolve(inputData)
        return adapterFn.invoke(listResult)
    }
}

