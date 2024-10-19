package po.api.rest_service.common

interface RestService<T> {
    fun create(resource: T): T
    fun update(id: Long, resource: T): T
    fun delete(id: Long): Boolean
    fun getById(id: Long): T?
    fun getAll(): List<T>
}