package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.dto.DTOClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

class DTORepo<ENTITY, CHILD_ENTITY>(
    val forProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
    val dtoModel: DTOClass<CHILD_ENTITY>,
)where ENTITY :LongEntity, CHILD_ENTITY: LongEntity {



}