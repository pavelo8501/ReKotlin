package po.exposify.scope.sequence.extensions

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.ClassSequenceHandler
import po.exposify.scope.sequence.classes.createHandler
import po.exposify.scope.sequence.models.SwitchData
import po.misc.collections.generateKey

suspend fun <DTO, D, E, F_DTO, FD, FE>  SequenceContext<DTO, D, E>.switch(
    dto: CommonDTO<DTO, D, E>,
    dtoClass: DTOClass<F_DTO, FD, FE>,
    block: suspend SequenceContext<F_DTO, FD, FE>.(ClassSequenceHandler<F_DTO, FD, FE>)-> Unit
) where  DTO: ModelDTO, D : DataModel, E : LongEntity,
         F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{

    val switchParameter =  this.sequenceHandler.switchParameters.firstOrNull {  it.childClass == dtoClass }
    switchParameter?.let {

        val repo = dto.getRepository(dtoClass,  Cardinality.ONE_TO_MANY)
        val newHandler = dtoClass.createHandler(this.sequenceHandler.sequenceId)
        val casted = it.castOrOperationsEx<SwitchData<F_DTO, FD, FE>>()
        casted.handlerBlock.invoke(newHandler)
        val newSequenceContext = SequenceContext(newHandler, repo as ExecutionContext<F_DTO, FD, FE>)
        casted.setSourceContext(newSequenceContext)
        newHandler.provideContext(newSequenceContext)
        block.invoke(newSequenceContext, newHandler)
    }?:run {
        throw OperationsException("SwitchParameters not found for class ${dtoClass.qualifiedName}", ExceptionCode.VALUE_NOT_FOUND)
    }

}