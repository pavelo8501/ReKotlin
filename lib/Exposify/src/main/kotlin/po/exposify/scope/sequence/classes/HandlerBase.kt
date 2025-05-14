package po.exposify.scope.sequence.classes

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.RootExecutionProvider
import po.exposify.dto.components.SimpleQuery
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.models.HandlerConfig



sealed class HandlerBase<DTO, D, E>(
    val dtoBase: DTOBase<DTO, D, E>,
    val cardinality: Cardinality
)  where DTO: ModelDTO, D: DataModel, E: LongEntity{

    internal var whereQueryParameter: SimpleQuery? = null
    val query: SimpleQuery get() = whereQueryParameter.getOrOperationsEx("Query parameter requested but uninitialized")

    internal val inputListSource : MutableList<D> = mutableListOf()
    val inputList : List<D>  get () = inputListSource.toList()

    internal var inputDataSource : D ?  =null
    val inputData: D  get() = inputDataSource?:throw OperationsException(
        "inputData used but not provided",
        ExceptionCode.VALUE_NOT_FOUND)


    internal val handlerConfig = HandlerConfig<DTO, D, E>(this)

    internal var collectListResultFn: ((ResultList<DTO, D, E>)-> Unit)? = null
    internal var collectSingleResultFn: ((ResultSingle<DTO, D, E>)-> Unit)? = null

    var finalResult: ResultList<DTO, D, E> = ResultList<DTO,D,E>(dtoBase)
    internal fun provideFinalResult(result : ResultList<DTO, D, E>){
        finalResult = result
    }

    fun collectResult(result : ResultList<DTO, D, E>):ResultList<DTO, D, E>{
        collectListResultFn?.invoke(result)
        return result
    }
    fun collectResult(result : ResultSingle<DTO, D, E>): ResultSingle<DTO, D, E>{
        collectSingleResultFn?.invoke(result)
        return result
    }

}

class Handler<DTO, D, E> (
    val dtoRoot: RootDTO<DTO, D, E>,
):HandlerBase<DTO, D, E>(dtoRoot, Cardinality.ONE_TO_MANY)
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    internal var handlerConfigFn :(suspend HandlerConfig<DTO, D, E>.()-> Unit)? = null
    internal val switchStatements : MutableMap<String, SwitchHandler<ModelDTO, DataModel, LongEntity, DTO, D, E>> = mutableMapOf()
    internal val handlerConfigStorage : MutableMap<String, (HandlerConfig<ModelDTO, DataModel, LongEntity>)-> Unit> = mutableMapOf()

    fun provideConfigFn(configFn : suspend HandlerConfig<DTO, D, E>.()-> Unit){
        handlerConfigFn = configFn
    }

    fun <F_DTO: ModelDTO, FD: DataModel, FE: LongEntity> configureHandler(name: String, handler: SwitchHandler<F_DTO, FD, FE, DTO, D,  E>){
        switchStatements[name] = handler.castOrOperationsEx<SwitchHandler<ModelDTO, DataModel, LongEntity, DTO, D, E>>()
        val configFn = handlerConfigStorage[name]
            .castOrOperationsEx<(HandlerConfig<F_DTO, FD, FE>)-> Unit>("No handler configuration provided for name:${name}")
        configFn.invoke(handler.handlerConfig)
    }

    var sequenceLambda: (suspend context(ResultSingle<DTO, D, E>)  SequenceContext<DTO, D, E>.(Handler<DTO, D, E>) -> ResultList<DTO, D, E>)? = null
    internal fun storeSequenceLambda( block: suspend context(ResultSingle<DTO, D, E>)  SequenceContext<DTO, D, E>.(Handler<DTO, D, E>) -> ResultList<DTO, D, E>){
        sequenceLambda = block
    }

   suspend fun  launch(): ResultList<DTO, D, E>{
       val execProvider = RootExecutionProvider(dtoRoot)
       val newContext = SequenceContext<DTO, D, E>(this, execProvider)
       return sequenceLambda.getOrOperationsEx("Sequence lambda is missing").invoke(newContext.lastResultProvider(), newContext, this)
    }

}

class SwitchHandler<DTO, D, E, F_DTO, FD, FE> (
    val dtoClass: DTOClass<DTO, D, E>,
    val parentHandler: Handler<F_DTO, FD, FE>,
    cardinality: Cardinality
):HandlerBase<DTO, D, E>(dtoClass, cardinality)
        where DTO: ModelDTO, D: DataModel, E: LongEntity,
              F_DTO : ModelDTO,FD : DataModel, FE: LongEntity
{

    private var handlerConfigFn : (suspend HandlerConfig<DTO, D, E>.()-> Unit)? = null
    fun configureHandler(name: String){
        parentHandler.configureHandler(name, this)
    }

    var switchArgument : ResultSingle<F_DTO, FD, FE>? = null
    var switchLambda: (suspend  SequenceContext<DTO, D, E>.(SwitchHandler<DTO, D, E, F_DTO, FD, FE>)-> ResultList<DTO, D, E>)? = null
    internal fun storeSwitchLambda(
        name: String,
        argument : ResultSingle<F_DTO, FD, FE>,
        block: suspend  SequenceContext<DTO, D, E>.(SwitchHandler<DTO, D, E, F_DTO, FD, FE>)-> ResultList<DTO, D, E>
    ){

        switchArgument = argument
        switchLambda = block
    }

    fun <F_DTO: ModelDTO, FD : DataModel, FE : LongEntity> provideConfigFn(
        name: String,
        configFn : suspend HandlerConfig<DTO, D, E>.()-> Unit
    ){
        handlerConfigFn = configFn
        parentHandler.handlerConfigStorage[name] = configFn.castOrOperationsEx<(HandlerConfig<ModelDTO, DataModel, LongEntity>)-> Unit>()
    }

    suspend fun launch(): ResultList<DTO, D, E> {
        val hostingDTO =  switchArgument.getOrOperationsEx("SwitchArgument is null").getAsCommonDTOForced()
        this.configureHandler("test")
        val repo = hostingDTO.getRepository(dtoClass, cardinality)
        val newSequenceContext = SequenceContext(this, repo.castOrOperationsEx())
        return switchLambda.getOrOperationsEx("SwitchLambda is null").invoke(newSequenceContext, this)
    }

}



