package po.exposify.scope.sequence.models

import kotlinx.coroutines.CompletableDeferred
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Op
import po.exposify.classes.interfaces.DataModel
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.SequenceHandler
import kotlin.reflect.KProperty1

data class SequencePack<DATA,ENTITY>(
    val context : SequenceContext<DATA,ENTITY>,
    val sequenceFn : suspend  SequenceContext<DATA, ENTITY>.() -> Unit,
    private val handler: SequenceHandler<DATA>,
) where  DATA : DataModel, ENTITY : LongEntity {
     val resultDeferred = CompletableDeferred<List<DATA>>()
   init {
       handler.onResultSubmitted {
           resultDeferred.complete(it)
       }
   }

    private var sequenceParams = mapOf<String, String>()
    private var sequenceInputList = listOf<DATA>()

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

    fun getInputList(): List<DATA>{
        return sequenceInputList
    }


    fun saveInputList(inputList : List<DATA>){
        sequenceInputList = inputList
    }

   suspend fun start(){
       println("Calling start in SequencePack")
       context.sequenceFn()
    }

   suspend fun onResult(): List<DATA> {
        return resultDeferred.await()
    }

   fun sequenceName(): String{
        return handler.name
    }
}
