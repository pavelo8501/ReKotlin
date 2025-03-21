package po.exposify.classes.components

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.sql.Op
import po.exposify.classes.interfaces.DataModel
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.sequence.models.SequencePack
import kotlin.reflect.KProperty1

class CallbackEmitter<DATA : DataModel> {

    private var onSequenceExecute
            :  (suspend (sequence : SequencePack<DATA,*>) -> Deferred<List<DATA>>)?  = null
    suspend fun launchSequence(sequence : SequencePack<DATA,*> ): Deferred<List<DATA>>{
        onSequenceExecute?.let {callback->
          return  callback.invoke(sequence)
        }?:run {
            return CompletableDeferred(emptyList())
        }
    }

    fun subscribeSequenceExecute(
        callback : (suspend (sequence : SequencePack<DATA,*>) -> Deferred<List<DATA>>)
    ){
        onSequenceExecute = callback
    }

}