package po.test.exposify.setup

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.dto.components.property_binder.bindings.ReferencedBinding
import po.exposify.dto.components.property_binder.bindings.SyncedBinding
import po.exposify.dto.components.property_binder.bindings.SerializedBinding
import po.exposify.dto.components.property_binder.delegates.referencing

@Serializable
data class TestUser(
    var login: String,
    var name: String,
    var email: String,
    var password: String,
): DataModel{
    override var id: Long = 0

    var created: LocalDateTime = TestUserDTO.nowTime()
    var updated: LocalDateTime = TestUserDTO.nowTime()
}


class TestUserDTO(
    override var dataModel: TestUser
): CommonDTO<TestUserDTO,  TestUser, TestUserEntity>(TestUserDTO) {

    companion object: DTOClass<TestUserDTO>(){
        override fun setup() {
            configuration<TestUser, TestUserEntity>(TestUserDTO::class, TestUserEntity){
                propertyBindings(
                    SyncedBinding(TestUser::login, TestUserEntity::login),
                    SyncedBinding(TestUser::name, TestUserEntity::name),
                    SyncedBinding(TestUser::email, TestUserEntity::email),
                    SyncedBinding(TestUser::password, TestUserEntity::password),
                    SyncedBinding(TestUser::created, TestUserEntity::created),
                    SyncedBinding(TestUser::updated, TestUserEntity::updated),
                )
            }
        }
    }
}


@Serializable
data class TestPage(
    var name: String,
    @SerialName("lang_id")
    var langId: Int,
    @SerialName("page_classes")
    var pageClasses: List<TestClassItem>,
    @SerialName("updated_by")
    var updatedById: Long
): DataModel{
    override var id: Long = 0

    var updated: LocalDateTime = TestPageDTO.nowTime()
    val sections: MutableList<TestSection> = mutableListOf<TestSection>()

}

class TestPageDTO(
    override var dataModel: TestPage
): CommonDTO<TestPageDTO, TestPage, TestPageEntity>(TestPageDTO) {


    var name by referencing (dataModel.name)

  //  var name by DataModelPropertyDelegate<TestPageDTO, TestPage, String>(TestPage::name)



    companion object: DTOClass<TestPageDTO>(){
        override fun setup() {
           configuration<TestPage, TestPageEntity>(TestPageDTO::class, TestPageEntity){
               propertyBindings(SyncedBinding(TestPage::name, TestPageEntity::name),
                   SyncedBinding(TestPage::langId,  TestPageEntity::langId),
                   SyncedBinding(TestPage::updated, TestPageEntity::updated),
                   SerializedBinding(TestPage::pageClasses, TestPageEntity::pageClasses, ListSerializer(TestClassItem.serializer())),
                   ReferencedBinding(TestPage::updatedById, TestPageEntity::updatedBy, TestUserDTO)
               )
               childBindings{
                   many<TestSectionDTO>(
                       childModel = TestSectionDTO,
                       ownDataModels = TestPage::sections,
                       ownEntities = TestPageEntity ::sections,
                       foreignEntity = TestSectionEntity::page
                   )
               }
           }
        }
    }
}

@Serializable
data class TestSection(
    var name: String,
    var description: String,
    @SerialName("json_ld")
    var jsonLd: String,
    @SerialName("section_classes")
    var sectionClasses: List<TestClassItem>,
    @SerialName("updated_by")
    var updatedById: Long,
    @SerialName("lang_id")
    var langId: Int,
    @SerialName("updated_By")
    var updatedBy: Long

): DataModel{
    override var id: Long = 0

    var updated: LocalDateTime = TestUserDTO.nowTime()
}

class TestSectionDTO(
    override var dataModel: TestSection
): CommonDTO<TestSectionDTO, TestSection, TestSectionEntity>(TestSectionDTO) {

    companion object: DTOClass<TestSectionDTO>(){

        override fun setup() {
            configuration<TestSection, TestSectionEntity>(TestSectionDTO::class, TestSectionEntity) {
                propertyBindings(
                    SyncedBinding(TestSection::name, TestSectionEntity::name),
                    SyncedBinding(TestSection::description, TestSectionEntity::description),
                    SyncedBinding(TestSection::jsonLd, TestSectionEntity::jsonLd),
                    SyncedBinding(TestSection::updated, TestSectionEntity::updated),
                    SyncedBinding(TestSection::langId, TestSectionEntity::langId ),
                    SerializedBinding(TestSection::sectionClasses, TestSectionEntity::sectionClasses, ListSerializer(TestClassItem.serializer())),
                    ReferencedBinding(TestSection::updatedById, TestSectionEntity::updatedBy, TestUserDTO),
                )
                childBindings {
                }
            }
        }
    }
}