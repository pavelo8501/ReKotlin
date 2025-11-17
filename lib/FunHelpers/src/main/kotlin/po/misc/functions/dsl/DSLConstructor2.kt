package po.misc.functions.dsl

import po.misc.functions.dsl.handlers.DefaultDSLHandler
import po.misc.functions.dsl.handlers.HandlerBase
import po.misc.functions.registries.DSLRegistry
import po.misc.types.token.TypeToken


@DSLBlockMarker
open class ContainingDSLBlock2<T: Any, P: Any>(
    val typeData: TypeToken<T>,
    private val block: T.(P)-> Unit,
) {

    private val notifierRegistry: DSLRegistry<T, HandlerBase<T>> = DSLRegistry(this, typeData, DefaultDSLHandler(typeData))

    val notifierCount : Int get() = notifierRegistry.subscriptionsCount

    val containerType:DSLContainerType get() =  DSLContainerType.OwnReceiver

    var invokeIfAdapterNull: Boolean = false
        internal set

}

class DSLConstructor2<T: Any,  R: Any>(
    private val constructLambda: (DSLConstructor2<T,R>.()-> Unit)? = null
){


}

