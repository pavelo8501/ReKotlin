package po.db.data_service.scope.service.controls.service_registry

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.classes.interfaces.DataModel

fun <DATA_MODEL, ENTITY> serviceRegistry(
    init: ServiceRegistryItemBuilder<DATA_MODEL, ENTITY>.() -> Unit
): ServiceRegistryItem<DATA_MODEL, ENTITY> where DATA_MODEL : DataModel, ENTITY : LongEntity {
    val builder = ServiceRegistryItemBuilder<DATA_MODEL, ENTITY>()
    builder.init()
    return builder.build()
}