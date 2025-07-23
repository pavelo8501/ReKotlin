package po.exposify.extensions

import po.exposify.dto.interfaces.DataModel
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException

fun checkDataListNotEmpty(dataModels: List<DataModel>){
    if( dataModels.isEmpty()){
        throw IllegalArgumentException("Provided DataModels input list is empty")
    }
}