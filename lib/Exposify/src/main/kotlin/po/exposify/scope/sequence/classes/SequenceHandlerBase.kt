package po.exposify.scope.sequence.classes

import org.jetbrains.exposed.dao.LongEntity
import po.auth.extensions.session
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.RootExecutionProvider
import po.exposify.dto.components.SimpleQuery
import po.exposify.dto.components.SwitchQuery
import po.exposify.dto.components.toResultList
import po.exposify.dto.components.toResultSingle
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.models.ClassHandlerConfig
import po.exposify.scope.sequence.models.HandlerConfigBase
import po.exposify.scope.sequence.models.RootHandlerConfig
import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler

sealed class SequenceHandlerBase<DTO, D, E>(
    val dtoBase: DTOBase<DTO, D, E>,
    val cardinality: Cardinality,
    val name : String,
) where DTO: ModelDTO, D: DataModel, E: LongEntity{

    abstract val handlerConfig: HandlerConfigBase<DTO, D, E>

    val inputList : List<D>  get () = handlerConfig.inputList
    val inputData: D  get() = handlerConfig.inputData
    val query: SimpleQuery get() = handlerConfig.query

    var finalResult: ResultList<DTO, D, E> = ResultList<DTO,D,E>(dtoBase)
    internal fun provideFinalResult(result : ResultList<DTO, D, E>){
        finalResult = result
    }

    internal fun provideCollectedResultSingle(result : ResultSingle<DTO,D,E>){
        handlerConfig.collectSingleResultFn?.invoke(result)
            ?:handlerConfig.collectListResultFn?.invoke(result.toResultList())
            ?: throw OperationsException("Result collection is required but onResultCollected lambda is not provided",
                ExceptionCode.VALUE_IS_NULL)
    }

    internal fun provideCollectedResultList(result : ResultList<DTO,D,E>){
        handlerConfig.collectListResultFn?.invoke(result)
            ?:handlerConfig.collectSingleResultFn?.invoke(result.toResultSingle())
            ?:throw OperationsException("Result collection is required but onResultCollected lambda is not provided",
                ExceptionCode.VALUE_IS_NULL)
    }

}

class RootSequenceHandler<DTO, D, E> (
    private val  handlerDelegate : RootHandlerProvider<DTO, D, E>,
    val dtoRoot: RootDTO<DTO, D, E>,
    name : String,
    val sequenceLambda:
        suspend context(AuthorizedSession)
            SequenceContext<DTO, D, E>.(RootSequenceHandler<DTO, D, E>) -> ResultList<DTO, D, E>

):SequenceHandlerBase<DTO, D, E>(dtoRoot, Cardinality.ONE_TO_MANY, name)
        where DTO: ModelDTO, D: DataModel, E: LongEntity
{
    override val handlerConfig : RootHandlerConfig<DTO, D, E> = RootHandlerConfig()
    private var classSequenceConfigurators : MutableMap<String, ClassHandlerConfig<*, *, *, DTO, D, E>.()-> Unit> = mutableMapOf()
    internal fun <F_DTO : ModelDTO, FD: DataModel, FE: LongEntity> provideClassSequenceConfigurator(
        handlerName: String,
        configurator : ClassHandlerConfig<DTO, D, E, F_DTO, FD, FE>.()-> Unit
    ){
        classSequenceConfigurators[handlerName] = configurator.castOrOperationsEx<ClassHandlerConfig<*, *, *, DTO, D, E>.()-> Unit>()
    }
    suspend fun launch(session: AuthorizedSession): ResultList<DTO, D, E>{
       val execProvider = RootExecutionProvider(dtoRoot)
       val newContext = SequenceContext(this, execProvider)
       return  sequenceLambda.invoke(session, newContext, this)
    }
}


class ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE> (
    val  handlerDelegate : SwitchHandlerProvider<DTO, D, E,  F_DTO, FD, FE>,
    val dtoClass: DTOClass<DTO, D, E>,
    cardinality: Cardinality,
    name : String
):SequenceHandlerBase<DTO, D, E>(dtoClass, cardinality, name)
        where DTO: ModelDTO, D: DataModel, E: LongEntity,
              F_DTO : ModelDTO,FD : DataModel, FE: LongEntity
{
    override val handlerConfig : ClassHandlerConfig<DTO, D, E, F_DTO, FD, FE> = ClassHandlerConfig()

    suspend fun launch(
        switchLambda :  suspend  SequenceContext<DTO, D, E>.(ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE>)-> ResultList<DTO, D, E>
        ): ResultList<DTO, D, E> {

        val switchQuery = handlerDelegate.switchQueryProvider.invoke()
        val hostingDTO = switchQuery.resolve().getAsCommonDTOForced()
        val repo = hostingDTO.getRepository(dtoClass, cardinality)
        val newSequenceContext = SequenceContext(this, repo)
        return switchLambda.invoke(newSequenceContext, this)
    }
}



