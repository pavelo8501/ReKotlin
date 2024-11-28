package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass



/*
    Interface used to identify DataClass with DataBase Entity
    Part of the property mapping system
 */
interface DTOMarker {

    val sysName : String

}

