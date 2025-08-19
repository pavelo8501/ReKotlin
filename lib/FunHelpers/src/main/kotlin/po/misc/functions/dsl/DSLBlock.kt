package po.misc.functions.dsl

import po.misc.functions.containers.Adapter
import po.misc.functions.containers.DSLProvider
import po.misc.types.getOrManaged


sealed class DSLBlockBase<T: Any, R: Any>(
    val lambda:(T.()->R)? = null
):DSLExecutableBlock<T, R>{

    protected var dSLProviderBacking: DSLProvider<T , R>? = null
    protected val dSLProvider: DSLProvider<T,R> get() =  dSLProviderBacking.getOrManaged("dSLProvider")
    val dslBlocks: List<DSLBlock<T, R>> = listOf()

    init {
        lambda?.let {
            dSLProviderBacking = DSLProvider(it)
        }
    }
}
@TopBlockDSLMarker()
class DSLBlock<T: Any, R: Any>(
    lambda:(T.()->R)? = null
):DSLBlockBase<T, R>(lambda){

    private val subContainers: MutableList<DSLConvertorBlock<*, T, R>> = mutableListOf()
//
//    val dslBlocksTotalSize: Int get() = subContainers.sumOf { it.dslBlocksTotalSize }
//    val subContainersSize: Int get()=  subContainers.size

    // override val dslBlocksTotalSize: Int get() = subContainers.sumOf { it.dslBlocksBacking.size } + dslBlocksBacking.size

    private fun provider():DSLProvider<T,R>{
        return dSLProvider.getOrManaged("dSLProvider")
    }

    fun evaluate(value:T):R{
        return  dSLProvider.trigger(value)
    }

//    fun<T2: Any> buildDSLConvertor2(adapterLambda: (T)->T2, lambda: DSLConvertorBlock<T2 ,T, R>.()-> Unit):DSLConvertorBlock<T2, T, R>{
//        val newConverter =  DSLConvertorBlock<T2, T, R>(this, adapterLambda)
//        newConverter.lambda()
//        subContainers.add(newConverter)
//        return newConverter
//    }
//
//    fun<T2: Any> buildDSLConvertor(adapterLambda: (T)->T2, lambda: T2.()->R):DSLConvertorBlock<T2, T, R>{
//        val newConverter = DSLConvertorBlock(this, adapterLambda, lambda)
//       subContainers.add(newConverter)
//       return newConverter
//    }

    fun build(lambda:T.()->R):DSLProvider<T,R>{
        val provider = DSLProvider(lambda)
        dSLProviderBacking = provider
        return provider
    }

}



@SubBlockDSLMarker()
class DSLConvertorBlock<T: Any, PT: Any, R: Any>(
   // val parentContainer: DSLStorage2<PT, R>,
    val adapterLambda: (PT)->T,
    lambda: (T.()->R)? = null,
) :DSLBlockBase<T, R>(lambda),DSLExecutableSubBlock<T, PT, R> {

    val adapter: Adapter<PT, T> = Adapter(adapterLambda)
//
//
//    private val dslBlocksBacking: MutableList<DSLBlock<T, R>> = mutableListOf()
//    override val dslBlocks: List<DSLBlock<T, R>> get() = dslBlocksBacking
//
//
//    fun build(adapterLambda: (PT)->T,  lambda: T.()->R): DSLBlock<T, R> {
//
//        val newDslBlock = DSLBlock<T, R>(this, lambda)
//
//        return newDslBlock
//    }
//
//    val dslBlocksTotalSize: Int get() = dslBlocks.sumOf { it.dslBlocksTotalSize } + dslBlocks.size
//
//    fun createDSLBlock(lambda: (T.()->R)? = null):DSLBlock<T, R>{
//       return DSLBlock<T,R>(this)
//    }
//
//    fun evaluate(value:PT):R{
//        val adapt = adapter.trigger(value)
//        val result = dSLProvider.trigger(adapt)
//        return  result
//    }
//
//    fun build(lambda:T.()->R){
//        val provider = DSLProvider(lambda)
//        dSLProviderBacking = provider
//    }

}

fun <T: Any, R: Any> DSLExecutableBlock<T, R>.nextBlock(block:T.()->R){
    (this as DSLBlock).build(block)
}

fun <T: Any, T2: Any, R: Any> DSLExecutableSubBlock<T, T2,  R>.nextBlock(block:T.()->R){

    //(this as DSLConvertorBlock).build(block)
}


inline fun <reified T2: Any, T: Any, R: Any> DSLExecutableBlock<T, R>.withBlock(
   noinline adapterLambda: (T)->T2,
    noinline block: DSLExecutableSubBlock<T2 ,T, R>.()-> Unit){

   // (this as DSLBlock).buildDSLConvertor2(adapterLambda, block)
}

inline fun <reified T2: Any, T: Any, R: Any> DSLExecutableBlock<T, R>.withBlock2(
    noinline adapterLambda: (T)->T2,
    noinline block: DSLExecutableSubBlock<T2 ,T, R>.()-> Unit):T.() -> R
{
//    val convertor =  (this as DSLBlock).buildDSLConvertor2(adapterLambda, block)
//   return convertor.lambda.getOrManaged("aa") as T.() -> R
    TODO("Ref")
}



