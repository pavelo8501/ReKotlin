package po.api.rest_service

import po.api.rest_service.common.ApiEntity
import po.api.rest_service.common.RestService


import io.ktor.server.engine.*
import io.ktor.server.netty.*

class KtorRestService : RestService<ApiEntity> {

    override fun create(resource: ApiEntity): ApiEntity {
        TODO("Not yet implemented")
    }

    override fun update(
        id: Long,
        resource: ApiEntity
    ): ApiEntity {
        TODO("Not yet implemented")
    }

    override fun delete(id: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun getById(id: Long): ApiEntity? {
        TODO("Not yet implemented")
    }

    override fun getAll(): List<ApiEntity> {
        TODO("Not yet implemented")
    }

}