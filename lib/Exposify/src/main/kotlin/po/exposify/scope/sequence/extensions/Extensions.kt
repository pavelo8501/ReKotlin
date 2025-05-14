package po.exposify.scope.sequence.extensions

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.Handler
import po.exposify.scope.sequence.classes.SwitchHandler
import po.exposify.scope.service.ServiceContext


context(serviceContext: ServiceContext<DTO, D, E>)
suspend fun <DTO, D, E> sequence(
    handler : Handler<DTO, D, E>,
    block: suspend context(ResultSingle<DTO, D, E>)  SequenceContext<DTO, D, E>.(Handler<DTO, D, E>) -> ResultList<DTO, D, E>
) where DTO: ModelDTO, D:DataModel, E:LongEntity
{
    handler.storeSequenceLambda(block)
}

suspend context(sequenceContext: SequenceContext<F_DTO, FD, FE>, switchArgument: ResultSingle<F_DTO, FD, FE>)
fun <DTO, D, E, F_DTO, FD, FE>   switchContext(
    handler : SwitchHandler<DTO, D, E, F_DTO,FD, FE>,
    block: suspend  SequenceContext<DTO, D, E>.(SwitchHandler<DTO, D, E, F_DTO,FD, FE>)-> ResultList<DTO, D, E>
)   where  DTO: ModelDTO, D : DataModel, E : LongEntity,
           F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{
    handler.storeSwitchLambda("test", switchArgument,  block)

}

fun <DTO, D, E>  ResultSingle<DTO, D, E>.toResultList(): ResultList<DTO, D, E> where  DTO: ModelDTO, D : DataModel, E : LongEntity{
    return ResultList(this.dtoClass).appendDto(this)
}


//suspend fun <DTO, D, E, F_DTO, FD, FE>  SequenceContext<DTO, D, E>.switch(
//    dto: CommonDTO<DTO, D, E>,
//    dtoClass: DTOClass<F_DTO, FD, FE>,
//    block: suspend SequenceContext<F_DTO, FD, FE>.(ClassSequenceHandler<F_DTO, FD, FE>)-> Unit
//) where  DTO: ModelDTO, D : DataModel, E : LongEntity,
//         F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
//{
//
//    val switchParameter =  this.sequenceHandler.switchParameters.firstOrNull {  it.childClass == dtoClass }
//    switchParameter?.let {
//
//        val repo = dto.getRepository(dtoClass,  Cardinality.ONE_TO_MANY)
//        val newHandler = dtoClass.createHandler(this.sequenceHandler.sequenceId)
//        val casted = it.castOrOperationsEx<SwitchData<F_DTO, FD, FE>>()
//        casted.handlerBlock.invoke(newHandler)
//        val newSequenceContext = SequenceContext(newHandler, repo as ExecutionContext<F_DTO, FD, FE>)
//        casted.setSourceContext(newSequenceContext)
//        newHandler.provideContext(newSequenceContext)
//        block.invoke(newSequenceContext, newHandler)
//    }?:run {
//        throw OperationsException("SwitchParameters not found for class ${dtoClass.qualifiedName}", ExceptionCode.VALUE_NOT_FOUND)
//    }
//
//}