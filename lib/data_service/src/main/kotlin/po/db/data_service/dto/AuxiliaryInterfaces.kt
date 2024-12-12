package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity

/*
    Interface used to identify Class as a DTO Entity
    Part of the property data handling system
 */
interface DTOEntityMarker<ENTITY> {
    val dataModelClassName : String
    fun setEntityDAO(entity : ENTITY)
}

/*
    Interface  identifying that Extending Class acts as a DataModel
    Part of the property data handling system
 */
interface DataModel<ENTITY> : DTOEntityMarker<ENTITY> {
    var id : Long
    val dataModel : Any
}