package po.test.exposify.setup.dtos

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.bindings.SyncedBinding
import po.test.exposify.setup.UserEntity


@Serializable
data class User(
    override var id: Long,
    override var login: String,
    override var hashedPassword: String,
    var name: String,
    override var email: String,
): DataModel, AuthenticationPrincipal{

    override var userGroupId: Long = 0L

    var created: LocalDateTime  = UserDTO.nowTime()
    var updated: LocalDateTime = UserDTO.nowTime()

    override fun asJson(): String {
        return Json.encodeToString(this)
    }
}

class UserDTO(
    override var dataModel: User
): CommonDTO<UserDTO,  User, UserEntity>(UserDTO) {

    companion object: RootDTO<UserDTO, User>(){

        override suspend fun setup() {

            configuration<UserDTO, User, UserEntity>(UserEntity){
                propertyBindings(
                    SyncedBinding(User::login, UserEntity::login),
                    SyncedBinding(User::name, UserEntity::name),
                    SyncedBinding(User::email, UserEntity::email),
                    SyncedBinding(User::hashedPassword, UserEntity::hashedPassword),
                    SyncedBinding(User::created, UserEntity::created),
                    SyncedBinding(User::updated, UserEntity::updated),
                )

            }
        }
    }
}