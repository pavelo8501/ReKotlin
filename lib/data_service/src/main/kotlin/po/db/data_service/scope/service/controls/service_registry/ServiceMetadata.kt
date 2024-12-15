package po.db.data_service.scope.service.controls.service_registry

class ServiceMetadataBuilder<DATA_MODEL : Any, ENTITY : Any> {
    var key: ServiceUniqueKey? = null
    var service: ServiceData<DATA_MODEL, ENTITY>? = null

    fun service(init: ServiceDataBuilder<DATA_MODEL, ENTITY>.() -> Unit) {
        val builder = ServiceDataBuilder<DATA_MODEL, ENTITY>()
        builder.init()
        service = builder.build()
    }

    fun build(): ServiceMetadata<DATA_MODEL, ENTITY> {
        requireNotNull(key) { "ServiceMetadata must have a key" }
        requireNotNull(service) { "ServiceMetadata must have a service" }
        return ServiceMetadata(key!!, service!!)
    }
}