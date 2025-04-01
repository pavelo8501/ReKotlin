package po.exposify.dto.models

import po.exposify.dto.enums.CrudType

data class CrudOperation(
    var type: CrudType,
    var complete: Boolean,
){

    fun setOperation(operationType : CrudType){
        complete = operationType == CrudType.NONE
        type = operationType
    }
}