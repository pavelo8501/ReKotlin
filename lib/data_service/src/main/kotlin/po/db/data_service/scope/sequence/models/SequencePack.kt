package po.db.data_service.scope.sequence.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.scope.sequence.SequenceContext

data class SequencePack<DATA,ENTITY>(
    val name : String,
    val context : SequenceContext<DATA,ENTITY>,
    val fn : suspend  SequenceContext<DATA, ENTITY>.()->Unit,
    ) where  DATA : DataModel, ENTITY : LongEntity