package po.db.data_service.dto


/*
    Interface used to identify Class as a DTO Entity
    Part of the property data handling system
 */
interface DTOEntityMarker<DATA_MODEL, ENTITY> {
    var id : Long
    val dataModelClassName : String
    fun setEntityDAO(entity : ENTITY)
}

/*
    Interface  identifying DTO Entity Class
    Part of the property data handling system
 */
interface DTOModel {
    val dtoModel : CommonDTO<*,*>
    val dataModel: DataModel
}

/*
    Interface  identifying DataModel Class
    Part of the property data handling system
 */
interface DataModel {
    var id : Long
}

/*
    Interface for common behaviour of classes hosting Notificator Class
 */
interface CanNotify{
    val name : String
}