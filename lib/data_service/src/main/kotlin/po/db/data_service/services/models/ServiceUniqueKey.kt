package po.db.data_service.services.models

import po.db.data_service.dto.AbstractDTOModel
import po.db.data_service.dto.DTOMarker
import kotlin.reflect.KClass

class ServiceUniqueKey(
    val serviceName: String,
    val dataModelClass: KClass<*>
)
//where  DATA_MODEL : DTOMarker
