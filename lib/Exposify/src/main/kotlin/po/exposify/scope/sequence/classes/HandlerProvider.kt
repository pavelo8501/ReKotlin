package po.exposify.scope.sequence.classes

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.SequenceContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


sealed interface HandlerProvider<DTO:ModelDTO, D: DataModel, E:LongEntity>{
    val dtoBase: DTOBase<DTO, D, E>
    var propertyName : String?
    val cardinality: Cardinality
    val rootHandler: Boolean
    var initialized: Boolean
}


sealed class HandlerProviderBase<DTO, D, E, V>(
    override val dtoBase: DTOBase<DTO, D, E>,
): ReadOnlyProperty<DTOBase<DTO, D, E>, V>,  HandlerProvider<DTO, D, E>
        where DTO: ModelDTO, D: DataModel, E: LongEntity, V : HandlerProvider<DTO, D, E>{

    override var propertyName : String? = null

    abstract override var initialized: Boolean
    abstract override var rootHandler : Boolean
    abstract override val cardinality: Cardinality
    abstract val nameUpdated: (String)-> Unit

    override fun getValue(thisRef: DTOBase<DTO, D, E>, property: KProperty<*>): V{
        val name = property.name
        propertyName = name

        return this as V
    }
}

class RootHandlerProvider<DTO, D, E>(
    val dtoRoot: RootDTO<DTO, D, E>,
):HandlerProviderBase<DTO, D, E, RootHandlerProvider<DTO, D, E>>(dtoRoot)
        where DTO: ModelDTO, D: DataModel, E: LongEntity{

    override var initialized: Boolean = false
    override var rootHandler : Boolean = true
    override val cardinality: Cardinality = Cardinality.ONE_TO_MANY
    override val nameUpdated: (String) -> Unit = { initialized = true }

    var sequenceLambda: (suspend context(AuthorizedSession)  SequenceContext<DTO, D, E>.(RootSequenceHandler<DTO, D, E>) -> ResultList<DTO, D, E>)? = null
    internal fun storeSequenceLambda(block: suspend context(AuthorizedSession)  SequenceContext<DTO, D, E>.(RootSequenceHandler<DTO, D, E>) -> ResultList<DTO, D, E>){
        sequenceLambda = block
    }

    internal fun createHandler(): RootSequenceHandler<DTO, D, E> {
        return RootSequenceHandler(this, dtoRoot, propertyName!!, sequenceLambda!!)
    }

}

class SwitchHandlerProvider<DTO, D, E, F_DTO, FD, FE>(
    val dtoClass: DTOClass<DTO, D, E>,
    override val cardinality: Cardinality,
    val rootHandlerDelegate: RootHandlerProvider<F_DTO, FD, FE> ,
):HandlerProviderBase<DTO, D, E, SwitchHandlerProvider<DTO, D, E, F_DTO, FD, FE>>(dtoClass)
        where DTO: ModelDTO, D: DataModel, E: LongEntity,
              F_DTO : ModelDTO,FD : DataModel, FE: LongEntity
{
    override var initialized: Boolean = false
    override var rootHandler : Boolean = false
    override val nameUpdated: (String) -> Unit = { initialized = true }

    var switchQueryBuilder : (()-> ResultSingle<F_DTO, FD, FE>)? = null
    var switchLambda: (suspend  SequenceContext<DTO, D, E>.(ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE>)-> ResultList<DTO, D, E>)? = null

    internal fun storeSwitchLambda(
        block: suspend  SequenceContext<DTO, D, E>.(ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE>)-> ResultList<DTO, D, E>
    ){
        switchLambda = block
    }

    internal fun createHandler():ClassSequenceHandler<DTO, D, E,  F_DTO, FD, FE> {
        val parentHandler = rootHandlerDelegate.createHandler()
        return ClassSequenceHandler(this, dtoClass,parentHandler, cardinality, propertyName!!, rootHandlerDelegate.sequenceLambda!!, switchLambda!!)
    }

}