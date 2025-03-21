package po.exposify.classes.components

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.sql.Op
import po.exposify.classes.interfaces.DataModel
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.sequence.models.SequencePack
import kotlin.reflect.KProperty1

class CallbackEmitter<DATA : DataModel> {


//    private var onSequenceLaunch
//        :  (suspend (handler : SequenceHandler<DATA>, conditions: Set<Op<Boolean>>, data : List<DATA>) -> Deferred<List<DATA>>)?  = null
//
//    fun subscribeOnSequenceLaunch(
//        callback : suspend (handler : SequenceHandler<DATA>, conditions: Set<Op<Boolean>>,  data : List<DATA>) ->  Deferred<List<DATA>>
//    ){
//        onSequenceLaunch = callback
//    }
//
//    suspend fun launchSequence(handler : SequenceHandler<DATA>, conditions: Set<Op<Boolean>>, data : List<DATA>): Deferred<List<DATA>>{
//        onSequenceLaunch?.let {
//          return it(handler, conditions,  data)
//       }?:run {
//           return CompletableDeferred(emptyList())
//       }
//    }

    private var onSequenceExecute
            :  (suspend (sequence : SequencePack<DATA,*>) -> Deferred<List<DATA>>)?  = null
    suspend fun launchSequence(sequence : SequencePack<DATA,*> ): Deferred<List<DATA>>{
        onSequenceExecute?.let {
          return  it.invoke(sequence)
        }?:run { return CompletableDeferred(emptyList()) }
    }

    fun subscribeSequenceExecute(
        callback : (suspend (sequence : SequencePack<DATA,*>) -> Deferred<List<DATA>>)
    ){
        onSequenceExecute = callback
    }



}