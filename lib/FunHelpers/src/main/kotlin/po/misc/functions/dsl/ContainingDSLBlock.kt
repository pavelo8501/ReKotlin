package po.misc.functions.dsl

import po.misc.exceptions.throwManaged
import po.misc.functions.containers.Adapter
import po.misc.functions.containers.DSLAdapter
import po.misc.functions.containers.DSLProvider
import po.misc.functions.dsl.handlers.DSLHandler
import kotlin.collections.mutableListOf

enum class DSLContainerType{
    OwnReceiver,
    ForeignReceiver
}




@DSLBlockMarker
open class ContainingDSLBlock<T: Any, R: Any, PT: Any>(
    private val block: (T.()->R)?,
    private val extractorFn:((PT)->T)? = null
) {
    private var extractor: Adapter<PT, T>? = null

    private var dslHandler : DSLHandler<T,R>? = null

    private val providersBacking: MutableList<DSLProvider<T, R>> = mutableListOf()
    val providers: List<DSLProvider<T, R>> = providersBacking

    private val adaptersBacking: MutableList<DSLAdapter<T, DSLHandler<T,R>, R>> = mutableListOf()
    val adapters: List<DSLAdapter<T, DSLHandler<T,R>, R>> = adaptersBacking

    val blocksCount : Int get() = adaptersBacking.size + providersBacking.size

    val containerType:DSLContainerType get() = extractor?.let {  DSLContainerType.ForeignReceiver }?: DSLContainerType.OwnReceiver

    var invokeIfAdapterNull: Boolean = false
        internal set

    constructor(block: T.(DSLHandler<T,R>)->R, handler: DSLHandler<T,R>,  extractorFn:((PT)->T)?) : this(null, extractorFn){
        dslHandler = handler
        registerAdapter(handler, block)
    }

    init {
        block?.let { registerProvider(it) }
        extractorFn?.let {
            extractor = createExtractor(it)
        }
    }

    private fun registerProvider(block:T.()->R){
        val provider =   DSLProvider(block)
        providersBacking.add(provider)
    }

    private fun registerAdapter(handler: DSLHandler<T,R>, block:T.(DSLHandler<T,R>)->R){
        val adapter =  DSLAdapter(handler,  block)
        adaptersBacking.add(adapter)
    }

    private fun createExtractor(extractor:(PT)->T):Adapter<PT, T>{
        return Adapter(extractor)
    }

    fun resolve(inputData: T) : List<R> {
        adapters.map { it.trigger(inputData) }
        var resultList = adapters.map { it.trigger(inputData) }
        dslHandler?.let { handler->
            resultList =  resultList.map { handler.transformResult(it) }
        }
        return resultList
    }

    fun resolve(inputData:T,  adapterFn : (List<R>) -> R): R{
        val listResult =  providers.map { it.trigger(inputData) }
        return adapterFn.invoke(listResult)
    }

    fun resolveWithParent(inputData: PT) : List<R> {

        val converted =  extractor?.trigger(inputData)?:throwManaged("resolveWithParent called but no adapter present", this)
        return  providers.map {provider->  provider.trigger(converted) }
    }

    fun resolveWithParent(inputData:PT,  adapterFn : (List<R>) -> R): R{
        val converted =  extractor?.trigger(inputData) ?:run {
            throwManaged("resolveWithParent called but no adapter present", this)
        }
        val listResult =  providers.map { it.trigger(converted) }
        return adapterFn.invoke(listResult)
    }
}

