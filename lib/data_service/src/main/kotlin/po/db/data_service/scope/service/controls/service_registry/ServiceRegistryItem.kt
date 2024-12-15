package po.db.data_service.scope.service.controls.service_registry

class ServiceRegistryItemBuilder<DATA_MODEL : Any, ENTITY : Any> {
    var key: ServiceUniqueKey? = null
    var metadata: ServiceMetadata<DATA_MODEL, ENTITY>? = null

    fun metadata(init: ServiceMetadataBuilder<DATA_MODEL, ENTITY>.() -> Unit) {
        val builder = ServiceMetadataBuilder<DATA_MODEL, ENTITY>()
        builder.init()
        metadata = builder.build()
    }

    fun build(): ServiceRegistryItem<DATA_MODEL, ENTITY> {
        requireNotNull(key) { "ServiceRegistryItem must have a key" }
        requireNotNull(metadata) { "ServiceRegistryItem must have metadata" }
        return ServiceRegistryItem(key!!, metadata!!)
    }
}