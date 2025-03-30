package po.exposify.classes.components

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.models.SequencePack2


class CallbackEmitter2<DTO : ModelDTO> {

    private var onSequenceExecute
            :  (suspend (sequence : SequencePack2<DTO>) -> Deferred<List<DataModel>>)?  = null
    suspend fun launchSequence(sequence : SequencePack2<DTO> ): Deferred<List<DataModel>>{
        onSequenceExecute?.let {callback->
            return  callback.invoke(sequence)
        }?:run {
            return CompletableDeferred(emptyList())
        }
    }

    fun subscribeSequenceExecute(
        callback : (suspend (sequence : SequencePack2<DTO>) -> Deferred<List<DataModel>>)
    ){
        onSequenceExecute = callback
    }

}