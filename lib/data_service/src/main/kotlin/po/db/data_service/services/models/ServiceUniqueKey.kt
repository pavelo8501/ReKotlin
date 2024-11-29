package po.db.data_service.services.models


import kotlin.reflect.KClass

class ServiceUniqueKey(
    val serviceName: String,
    val dataModelClass: KClass<*>
)
//where  DATA_MODEL : DTOMarker
