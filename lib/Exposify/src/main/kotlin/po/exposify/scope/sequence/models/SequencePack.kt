package po.exposify.scope.sequence.models

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Op
import po.exposify.classes.interfaces.DataModel
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.service.ServiceClass
import kotlin.reflect.KProperty1

data class SequencePack<DATA,ENTITY>(
    private val context : SequenceContext<DATA,ENTITY>,
    internal val serviceClass: ServiceClass<DATA, ENTITY>,
    private val sequenceFn : suspend  SequenceContext<DATA, ENTITY>.() -> Unit,
    private val handler: SequenceHandler<DATA, ENTITY>,
) where  DATA : DataModel, ENTITY : LongEntity {

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

   suspend fun start(): Deferred<List<DATA>>{
       println("Calling start in SequencePack")
       // sequenceFn.invoke()
       val deferred =  context.checkout()
       sequenceFn(context)
       return  deferred
   }

   fun sequenceName(): String{
        return handler.name
    }
}
