package po.exposify.dto.components.bindings

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.executioncontext.DTOExecutionContext

import po.exposify.dto.components.executioncontext.RootExecutionContext
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO



internal inline fun <DTO: ModelDTO, D: DataModel, E: LongEntity, R> RootExecutionContext<DTO, D, E>.withDTOHub(
    commonDTO: CommonDTO<DTO, D, E>,
    block: BindingHub<DTO, D, E>.(CommonDTO<DTO, D, E>)->R
):R {
   return commonDTO.bindingHub.block(commonDTO)
}


internal inline fun <DTO, D, E, F, FD, FE,  R> DTOExecutionContext<DTO, D, E, F, FD, FE>.withDTOHub(
    commonDTO: CommonDTO<F, FD, FE>,
    block: BindingHub<F, FD, FE>.(CommonDTO<F, FD, FE>)->R
):R where DTO: ModelDTO, D: DataModel, E: LongEntity, F:ModelDTO, FD:DataModel, FE:LongEntity{

    return commonDTO.bindingHub.block(commonDTO)

}

internal inline fun <DTO, D, E, F, FD, FE,  R> DTOExecutionContext<DTO, D, E, F, FD, FE>.withHostDTOHub(
    block: BindingHub<DTO, D, E>.()->R
):R where DTO: ModelDTO, D: DataModel, E: LongEntity, F:ModelDTO, FD:DataModel, FE:LongEntity{

   return hostDTO.bindingHub.block()
}

