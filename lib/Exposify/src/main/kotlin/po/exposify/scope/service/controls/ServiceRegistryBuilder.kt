package po.exposify.scope.service.controls

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.interfaces.DataModel
import po.exposify.scope.service.controls.service_registry.ServiceRegistryItem
import po.exposify.scope.service.controls.service_registry.ServiceRegistryItemBuilder
import po.exposify.scope.service.controls.service_registry.serviceRegistry


class ServiceRegistryBuilder<DATA_MODEL : DataModel, ENTITY : LongEntity>
    : ListBuilder<ServiceRegistryItem<DATA_MODEL, ENTITY>>() {
    fun addServiceRegistryItem(
        init: ServiceRegistryItemBuilder<DATA_MODEL, ENTITY>.() -> Unit
    ) = apply {
        val serviceRegistryItem = serviceRegistry(init)
        add(serviceRegistryItem)
    }

    fun validateUniqueKeys() {
        val duplicateKeys = list.groupBy { it.key }.filter { it.value.size > 1 }
        require(duplicateKeys.isEmpty()) {
            "Duplicate keys found: ${duplicateKeys.keys}"
        }
    }
}