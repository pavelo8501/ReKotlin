package po.exposify.classes.components

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import po.exposify.classes.interfaces.DataModel
import po.exposify.scope.sequence.classes.SequenceHandler
import kotlin.reflect.KProperty1

class CallbackEmitter<DATA : DataModel> {

    var onSequenceLaunch: (suspend (sequenceName : String, data: List<*>?)-> Unit)? = null
    suspend fun <DATA: DataModel> callOnSequenceLaunch(sequenceName: String, data : List<DATA>? = null){
        onSequenceLaunch?.invoke(sequenceName, data)
    }

    private var onSequenceLaunchRequest
        :  (suspend (handler : SequenceHandler<DATA>,conditions: List<Pair<KProperty1<DATA, *>, Any?>>, data : List<DATA>) -> Deferred<List<DATA>>)?  = null

    fun subscribeOnSequenceLaunchRequest(
        callback : suspend (handler : SequenceHandler<DATA>, conditions: List<Pair<KProperty1<DATA, *>, Any?>>,  data : List<DATA>) ->  Deferred<List<DATA>>
    ){
        onSequenceLaunchRequest = callback
    }

    suspend fun launchSequence(handler : SequenceHandler<DATA>, conditions: List<Pair<KProperty1<DATA, *>, Any?>>, data : List<DATA>): Deferred<List<DATA>>{
       onSequenceLaunchRequest?.let { callback->
          return callback(handler, conditions,  data)
       }?:run {
           return CompletableDeferred(emptyList())
       }
    }

}