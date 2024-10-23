package po.api.rest_service.common

interface ApiEntity {
    val id: Long
}

interface ApiUpdateEntity {
    val id: Long
    val resource :  ApiEntity
}


interface ApiLoginRequestDataContext {

    val username: String
    val password: String

}