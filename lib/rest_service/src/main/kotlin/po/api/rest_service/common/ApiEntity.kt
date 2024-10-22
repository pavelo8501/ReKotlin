package po.api.rest_service.common

interface ApiEntity {
    val id: Long
}

interface ApiUpdateEntity {
    //ID for the entity to be updated
    val id: Long
    //The resource to be updated from represented by the entity
    val resource :  ApiEntity
}

interface ApiDeleteEntity {

    //ID for the entity to be deleted
    val id: Long
    val includingChild: Boolean

}

interface ApiLoginRequestDataContext {

    val username: String
    val password: String

}