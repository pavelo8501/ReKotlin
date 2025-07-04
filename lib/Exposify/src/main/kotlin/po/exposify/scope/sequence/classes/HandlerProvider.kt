package po.exposify.scope.sequence.classes

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.SwitchQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrInit
import po.exposify.extensions.getOrOperations
import po.exposify.scope.sequence.SequenceContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


sealed interface HandlerProvider<DTO:ModelDTO, D: DataModel, E:LongEntity>{
    val dtoBase: DTOBase<DTO, D, E>
    val name : String
    val cardinality: Cardinality
    val isRootHandler: Boolean
    var isInitialized: Boolean
}


sealed class HandlerProviderBase<DTO, D, E, V>(
    override val dtoBase: DTOBase<DTO, D, E>,
): ReadOnlyProperty<DTOBase<DTO, D, E>, V>,  HandlerProvider<DTO, D, E>
        where DTO: ModelDTO, D: DataModel, E: LongEntity, V : HandlerProvider<DTO, D, E>{

    private var propertyName : String? = null
    override val name : String get() = propertyName.getOrInit("propertyName")

    override var isInitialized: Boolean = false
    abstract override var isRootHandler : Boolean
    abstract override val cardinality: Cardinality
    abstract val nameUpdated: (String)-> Unit

    override fun getValue(thisRef: DTOBase<DTO, D, E>, property: KProperty<*>): V{
        val name = property.name
        propertyName = name
        return this as V
    }
}

class RootHandlerProvider<DTO, D, E> internal constructor(
    val dtoRoot: RootDTO<DTO, D, E>,
):HandlerProviderBase<DTO, D, E, RootHandlerProvider<DTO, D, E>>(dtoRoot)
        where DTO: ModelDTO, D: DataModel, E: LongEntity{

    override var isRootHandler : Boolean = true
    override val cardinality: Cardinality = Cardinality.ONE_TO_MANY
    override val nameUpdated: (String) -> Unit = { isInitialized = true }

    private var sequenceLambdaParameter:
            (suspend SequenceContext<DTO, D, E>.(RootSequenceHandler<DTO, D, E>) -> ResultList<DTO, D, E>)? = null

    val sequenceLambda :
            suspend SequenceContext<DTO, D, E>.(RootSequenceHandler<DTO, D, E>) -> ResultList<DTO, D, E>
        get() = sequenceLambdaParameter.getOrInit("sequenceLambdaParameter")

    internal fun storeSequenceLambda(block: suspend  SequenceContext<DTO, D, E>.(RootSequenceHandler<DTO, D, E> ) -> ResultList<DTO, D, E>){
        dtoRoot.debug.notify("Sequence lambda saved")
        sequenceLambdaParameter = block
    }

    internal fun createHandler(): RootSequenceHandler<DTO, D, E> {
        return RootSequenceHandler(this, dtoRoot, name, sequenceLambda)
    }
}

class SwitchHandlerProvider<DTO, D, E, F_DTO, FD, FE> internal constructor(
    val dtoClass: DTOClass<DTO, D, E>,
    override val cardinality: Cardinality,
    val rootSequenceHandler: RootHandlerProvider<F_DTO, FD, FE> ,
):HandlerProviderBase<DTO, D, E, SwitchHandlerProvider<DTO, D, E, F_DTO, FD, FE>>(dtoClass)
        where DTO: ModelDTO, D: DataModel, E: LongEntity,
              F_DTO : ModelDTO,FD : DataModel, FE: LongEntity
{

    override var isRootHandler : Boolean = false
    override val nameUpdated: (String) -> Unit = { isInitialized = true }

    private var switchQueryProviderParameter : (()-> SwitchQuery<F_DTO, FD, FE>)? = null
    val  switchQueryProvider : ()-> SwitchQuery<F_DTO, FD, FE>
        get() = switchQueryProviderParameter.getOrOperations("switchQueryProviderParameter")

    var switchLambdaParameter: (suspend  SequenceContext<DTO, D, E>.(ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE>)-> ResultList<DTO, D, E>)? = null
    val switchLambda: (suspend  SequenceContext<DTO, D, E>.(ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE>)-> ResultList<DTO, D, E>)
        get() =  switchLambdaParameter.getOrInit("switchLambdaParameter")

    internal fun storeSwitchLambda(
        block: suspend  SequenceContext<DTO, D, E>.(ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE>)-> ResultList<DTO, D, E>
    ){
        switchLambdaParameter = block
    }

    internal fun createHandler(
        switchQueryProvider:  ()-> SwitchQuery<F_DTO, FD, FE>
    ):ClassSequenceHandler<DTO, D, E,  F_DTO, FD, FE> {
        this.switchQueryProviderParameter = switchQueryProvider
        return ClassSequenceHandler(this, dtoClass, cardinality, name)
    }

    internal fun createParentHandler(): RootSequenceHandler<F_DTO, FD, FE> {
       return   rootSequenceHandler.createHandler()
    }

}