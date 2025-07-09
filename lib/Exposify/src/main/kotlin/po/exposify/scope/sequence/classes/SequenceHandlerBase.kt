package po.exposify.scope.sequence.classes

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.SimpleQuery
import po.exposify.dto.components.createProvider
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResultSingle
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SequenceRunInfo
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.getOrOperations
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.models.ClassHandlerConfig
import po.exposify.scope.sequence.models.HandlerConfigBase
import po.exposify.scope.sequence.models.RootHandlerConfig
import po.misc.interfaces.IdentifiableContext


@Deprecated("Will get depreciated", ReplaceWith("ExecutionHandler"), DeprecationLevel.WARNING)
sealed class SequenceHandlerBase<DTO, D, E>(
    val dtoBase: DTOBase<DTO, D, E>,
    val cardinality: Cardinality,
    val name : String,
) where DTO: ModelDTO, D: DataModel, E: LongEntity{

    abstract val handlerConfig: HandlerConfigBase<DTO, D, E>

    val inputList : List<D>  get () = handlerConfig.inputList
    val inputData: D  get() = handlerConfig.inputData
    val query: SimpleQuery get() = handlerConfig.query

    private var finalResultParameter: ResultList<DTO, D, E>? = null
    val finalResult: ResultList<DTO, D, E>
        get()= finalResultParameter.getOrOperations("finalResult")

    internal fun provideFinalResult(result : ResultList<DTO, D, E>){
        finalResultParameter = result
    }

    internal fun provideFinalResult(result : ResultSingle<DTO, D, E>){
        finalResultParameter = result.toResultList()
    }

    internal fun provideCollectedResultSingle(result : ResultSingle<DTO,D,E>){
        handlerConfig.collectSingleResultFn?.invoke(result)
            ?:handlerConfig.collectListResultFn?.invoke(result.toResultList())
            ?: throw operationsException("Result collection is required but onResultCollected lambda is not provided", ExceptionCode.VALUE_IS_NULL)
    }

    internal fun provideCollectedResultList(result : ResultList<DTO,D,E>){
        handlerConfig.collectListResultFn?.invoke(result)
            ?:handlerConfig.collectSingleResultFn?.invoke(result.toResultSingle())
            ?: throw operationsException(
                "Result collection is required but onResultCollected lambda is not provided",
                ExceptionCode.VALUE_IS_NULL)
    }
}

class RootSequenceHandler<DTO, D, E> (
    private val  handlerDelegate : RootHandlerProvider<DTO, D, E>,
    val dtoRoot: RootDTO<DTO, D, E>,
    name : String,
    val sequenceLambda: suspend SequenceContext<DTO, D, E>.(RootSequenceHandler<DTO, D, E>) -> ResultList<DTO, D, E>
):SequenceHandlerBase<DTO, D, E>(dtoRoot, Cardinality.ONE_TO_MANY, name), IdentifiableContext
        where DTO: ModelDTO, D: DataModel, E: LongEntity {
    override val contextName: String
        get() = "RootSequenceHandler"

    override val handlerConfig: RootHandlerConfig<DTO, D, E> = RootHandlerConfig()
    private var classSequenceConfigurators: MutableMap<String, ClassHandlerConfig<*, *, *, DTO, D, E>.() -> Unit> =
        mutableMapOf()
    var lastActiveSequenceContext: SequenceContext<DTO, D, E>? = null


//    internal fun <F_DTO : ModelDTO<F_DTO>, FD: DataModel, FE: LongEntity> provideClassSequenceConfigurator(
//        handlerName: String,
//        configurator : ClassHandlerConfig<DTO, D, E, F_DTO, FD, FE>.()-> Unit
//    ){
//        classSequenceConfigurators.put(handlerName, configurator)
//    }


    suspend fun launch(runInfo: SequenceRunInfo): ResultList<DTO, D, E> {

//        val execProvider = dtoRoot.createProvider(this)
//        lastActiveSequenceContext = SequenceContext(this, execProvider, runInfo)
//        return lastActiveSequenceContext.getOrOperations(this).let {
//            sequenceLambda.invoke(it, this)
//        }

        TODO("To depreciate")
    }
}


class ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE> (
    val  handlerDelegate : SwitchHandlerProvider<DTO, D, E,  F_DTO, FD, FE>,
    val dtoClass: DTOClass<DTO, D, E>,
    cardinality: Cardinality,
    name : String
):SequenceHandlerBase<DTO, D, E>(dtoClass, cardinality, name)
        where DTO: ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO, FD : DataModel, FE: LongEntity {
    override val handlerConfig: ClassHandlerConfig<DTO, D, E, F_DTO, FD, FE> = ClassHandlerConfig()

    suspend fun launch(
        runInfo: SequenceRunInfo,
        switchLambda: suspend SequenceContext<DTO, D, E>.(ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE>) -> ResultList<DTO, D, E>
    ): ResultList<DTO, D, E> {
        val switchQuery = handlerDelegate.switchQueryProvider.invoke()

      //  val newSequenceContext = SequenceContext(this)

       // val newSequenceContext = SequenceContext(this)
        //val result = switchLambda.invoke(newSequenceContext, this)

       // return result
        TODO("To refactor")
    }
}



