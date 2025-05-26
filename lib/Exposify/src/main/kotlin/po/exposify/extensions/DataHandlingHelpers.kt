package po.exposify.extensions

import po.exposify.dto.interfaces.DataModel
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode

fun checkDataListNotEmpty(dataModels: List<DataModel>){
    if( dataModels.isEmpty()){
        throw OperationsException("Provided DataModels input list is empty", ExceptionCode.INVALID_DATA)
    }
}