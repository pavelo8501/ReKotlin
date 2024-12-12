package po.db.data_service.dto

/*
    Interface used to identify Class as a DTO Entity
    Part of the property data handling system
 */
interface DTOEntityMarker {
    val sysName : String
}

/*
    Interface  identifying that Extending Class acts as a DataModel
    Part of the property data handling system
 */
interface DataModel : DTOEntityMarker {
    var id : Long
}