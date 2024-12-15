package po.db.data_service.scope.service.controls.service_registry



fun <DATA_MODEL : Any, ENTITY : Any> serviceRegistry(
    init: ServiceRegistryItemBuilder<DATA_MODEL, ENTITY>.() -> Unit
): ServiceRegistryItem<DATA_MODEL, ENTITY> {
    val builder = ServiceRegistryItemBuilder<DATA_MODEL, ENTITY>()
    builder.init()
    return builder.build()
}