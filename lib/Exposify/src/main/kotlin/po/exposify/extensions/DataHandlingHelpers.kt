package po.exposify.extensions

import po.exposify.dto.interfaces.DataModel
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.throwOperations

fun checkDataListNotEmpty(dataModels: List<DataModel>){
    if( dataModels.isEmpty()){
        throwOperations("Provided DataModels input list is empty", ExceptionCode.INVALID_DATA)
    }
}