package po.exposify.dto.helpers

import po.exposify.dto.CommonDTO
import po.exposify.dto.enums.DataStatus
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrOperations
import po.misc.context.CTX

fun <DTO, D> CommonDTO<DTO, D, *>.asDTO():DTO where DTO : ModelDTO, D : DataModel{
    return castOrOperations(commonType.dtoType.kClass, this)
}


internal fun checkDtoId(callingContext: CTX,  commonDTO: CommonDTO<*,*,*>): Long{
    return if(commonDTO.dataStatus != DataStatus.PreflightCheckMock){
        if(commonDTO.id >= 0L){
            commonDTO.id
        }else{
            var msg = "Check for dto id  failure. Dto: ${commonDTO.identifiedByName} in ${callingContext.identifiedByName}"
            msg += "Id is ${commonDTO.id}"
            throw  callingContext.operationsException(msg, ExceptionCode.ABNORMAL_STATE)
        }
    }else{
        commonDTO.id
    }
}

internal fun CTX.checkId(id:Long): Long{
    val message = "Check for raw id failure. id: $id in $identifiedByName"
    if(id >= 0L){
        return id
    }else{
        throw  operationsException(message, ExceptionCode.ABNORMAL_STATE)
    }

}