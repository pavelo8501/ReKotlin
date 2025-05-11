package po.exposify.scope.sequence.extensions

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOClass
import po.exposify.dto.components.ClassExecutionProvider
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.ClassSequenceHandler
import po.exposify.scope.sequence.models.SwitchData

suspend fun <DTO: ModelDTO, D : DataModel, E : LongEntity, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity>  SequenceContext<DTO, D, E>.switch(
    childDtoClass: DTOClass<F_DTO, FD, FE>,
    block: suspend SequenceContext<F_DTO, FD, FE>.(ClassSequenceHandler<F_DTO, FD, FE>)-> Unit
){

    val switchParameter =  this.handler.switchParameters.firstOrNull { it.handler.dtoClass == childDtoClass }
            .castOrOperationsEx<SwitchData<DTO, D, E, F_DTO, FD, FE>>()

    val switchDto = switchParameter.query.resolveQuery().getOrOperationsEx("Unable to find dto with given id : ${switchParameter.query.id}")
    val newExecutionContext =  switchDto.executionProvider(childDtoClass)

 //   val newExecutionContext = ClassExecutionProvider(switchDto, childDtoClass)

    val newSequenceContext = SequenceContext(switchParameter.handler, newExecutionContext as ExecutionContext<F_DTO, FD, FE>)
    block.invoke(newSequenceContext, switchParameter.handler)
}