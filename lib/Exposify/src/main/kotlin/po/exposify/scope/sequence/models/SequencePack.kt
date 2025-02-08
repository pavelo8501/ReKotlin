package po.exposify.scope.sequence.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.interfaces.DataModel
import po.exposify.scope.sequence.SequenceContext

data class SequencePack<DATA,ENTITY>(
    val context : SequenceContext<DATA,ENTITY>,
    val sequenceFn : suspend  SequenceContext<DATA, ENTITY>.(List<DATA>?) ->Unit,
    ) where  DATA : DataModel, ENTITY : LongEntity {

   suspend fun start(withData : List<DATA>?){
       println("Calling start in SequencePack")
       context.sequenceFn(withData)
       println("context.fn() invoked in SequencePack")
    }
}