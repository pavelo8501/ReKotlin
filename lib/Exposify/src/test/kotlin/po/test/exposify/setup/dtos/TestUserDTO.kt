package po.test.exposify.setup.dtos

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.bindings.SyncedBinding
import po.test.exposify.setup.TestUserEntity



@Serializable
data class TestUser(
    override var id: Long,
    override var login: String,
    override var hashedPassword: String,
    var name: String,
    override var email: String,
): DataModel, AuthenticationPrincipal{

    override var userGroupId: Long = 0L

    var created: LocalDateTime  = TestUserDTO.nowTime()
    var updated: LocalDateTime = TestUserDTO.nowTime()

    override fun asJson(): String {
        return Json.encodeToString(this)
    }
}

class TestUserDTO(
    override var dataModel: TestUser
): CommonDTO<TestUserDTO,  TestUser, TestUserEntity>(TestUserDTO) {

    companion object: DTOClass<TestUserDTO>(){
        override suspend fun setup() {
            configuration<TestUserDTO, TestUser, TestUserEntity>(TestUserEntity){
                propertyBindings(
                    SyncedBinding(TestUser::login, TestUserEntity::login),
                    SyncedBinding(TestUser::name, TestUserEntity::name),
                    SyncedBinding(TestUser::email, TestUserEntity::email),
                    SyncedBinding(TestUser::hashedPassword, TestUserEntity::hashedPassword),
                    SyncedBinding(TestUser::created, TestUserEntity::created),
                    SyncedBinding(TestUser::updated, TestUserEntity::updated),
                )
            }
        }
    }
}