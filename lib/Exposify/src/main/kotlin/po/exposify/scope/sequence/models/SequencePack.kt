package po.exposify.scope.sequence.models

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.interfaces.DataModel
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.SequenceHandler

data class SequencePack<DATA,ENTITY>(
    val context : SequenceContext<DATA,ENTITY>,
    val sequenceFn : suspend  SequenceContext<DATA, ENTITY>.(List<DATA>) -> Unit,
    private val handler: SequenceHandler<DATA>,
) where  DATA : DataModel, ENTITY : LongEntity {

    val resultDeferred = CompletableDeferred<List<DATA>>()

   init {
       handler.onResultSubmitted {
           resultDeferred.complete(it)
       }
   }

   suspend fun start(data : List<DATA>){
       println("Calling start in SequencePack")
       context.sequenceFn(data)
    }

    suspend fun onResult(): List<DATA> {
        return resultDeferred.await()
    }

    fun sequenceName(): String{
        return handler.name
    }
}