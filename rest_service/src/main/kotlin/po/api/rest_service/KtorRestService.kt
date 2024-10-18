package po.api.rest_service

import po.api.rest_service.common.ApiEntity
import po.api.rest_service.common.RestService

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class KtorRestService : RestService<ApiEntity> {

    private val client = HttpClient()


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