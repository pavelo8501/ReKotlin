package po.test.exposify.setup.dtos

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.bindings.SyncedBinding
import po.exposify.dto.components.property_binder.delegates.binding
import po.test.exposify.setup.SectionEntity
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

    var login : String by binding(User::login, UserEntity::login)
    var name : String by binding(User::name, UserEntity::name)
    var email : String by binding(User::email, UserEntity::email)
    var hashedPassword : String by binding(User::hashedPassword, UserEntity::hashedPassword)
    var updated : LocalDateTime by binding(User::updated, UserEntity::updated)
    var created : LocalDateTime by binding(User::created, UserEntity::created)

    companion object: RootDTO<UserDTO, User, UserEntity>(){
        override suspend fun setup() {
            configuration<UserDTO, User, UserEntity>(UserEntity){

            }
        }
    }
}