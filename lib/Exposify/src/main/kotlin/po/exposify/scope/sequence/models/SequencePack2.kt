package po.exposify.scope.sequence.models

import kotlinx.coroutines.Deferred
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.scope.sequence.SequenceContext2
import po.exposify.scope.sequence.classes.SequenceHandler2
import po.exposify.scope.service.ServiceClass


data class SequencePack2<DTO>(
    private val context : SequenceContext2<DTO>,
    internal val serviceClass: ServiceClass<DTO, *, *>,
    private val sequenceFn : suspend  SequenceContext2<DTO>.() -> Unit,
    private val handler: SequenceHandler2<DTO>,
) where  DTO : ModelDTO {

    private var sequenceParams = mapOf<String, String>()
    private var sequenceInputList = listOf<DataModel>()

    fun saveParams(params: Map<String, String>){
        sequenceParams = params
    }

    fun getParam(key: String): String{
        val value  = sequenceParams[key]
        if(value != null){
            return  value
        }else{
            throw OperationsException("Parameter with key $key not found in sequence ${this.sequenceName()}",
                ExceptionCodes.KEY_NOT_FOUND)
        }
    }

    fun getInputList(): List<DataModel>{
        return sequenceInputList
    }

    fun saveInputList(inputList : List<DataModel>){
        sequenceInputList = inputList
    }

    suspend fun start(): Deferred<List<DataModel>>{
        context.sequenceFn()
        val deferred =  context.checkout()
        return  deferred
    }

    fun sequenceName(): String{
        return handler.thisKey
    }
}
