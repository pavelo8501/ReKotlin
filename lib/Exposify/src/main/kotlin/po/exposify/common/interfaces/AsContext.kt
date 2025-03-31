package po.exposify.common.interfaces

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.interfaces.DataModel
import kotlin.reflect.KClass

interface AsContext<DATA: DataModel> {
   // val dataModel: DATA
}