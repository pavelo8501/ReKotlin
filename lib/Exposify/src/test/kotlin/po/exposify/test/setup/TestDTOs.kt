package po.exposify.test.setup

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import po.exposify.dto.components.ReadOnly
import po.exposify.dto.components.SyncedBinding
import po.exposify.dto.components.SyncedSerialized
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass

@Serializable
data class TestUser(
    var login: String,
    var name: String,
    var email: String,
    var password: String,
    var created: LocalDateTime,
    var updated: LocalDateTime
): DataModel{
    override var id: Long = 0
}


class TestUserDTO(
    override var dataModel: TestUser
): CommonDTO<TestUserDTO,  TestUser, TestUserEntity>(TestUserDTO) {

    companion object: DTOClass<TestUserDTO>(){
        override fun setup() {
            configuration<TestUser, TestUserEntity>(TestUserDTO::class, TestUserEntity){
                propertyBindings(
                    SyncedBinding(TestUser::name, TestUserEntity::name)
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
): DataModel{
    override var id: Long = 0
    var updated: LocalDateTime = TestPageDTO.nowTime()
    val sections: MutableList<TestSection> = mutableListOf<TestSection>()
}

class TestPageDTO(
    override var dataModel: TestPage
): CommonDTO<TestPageDTO, TestPage, TestPageEntity>(TestPageDTO) {
    companion object: DTOClass<TestPageDTO>(){
        override fun setup() {
           configuration<TestPage, TestPageEntity>(TestPageDTO::class, TestPageEntity){
               propertyBindings(SyncedBinding(TestPage::name, TestPageEntity::name),
                    SyncedBinding(TestPage::langId,  TestPageEntity::langId),
                    SyncedBinding(TestPage::updated, TestPageEntity::updated),
                    SyncedSerialized(TestPage::pageClasses, TestPageEntity::pageClasses, ListSerializer(TestClassItem.serializer())),
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
    @SerialName("class_list")
    var classList: List<TestClassItem>,
 //   @SerialName("meta_tags")
 //   var metaTags: List<TestMetaTag>,
    @SerialName("lang_id")
    var langId: Int,
    @SerialName("page_id")
    var pageId: Long
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
                    SyncedSerialized(TestSection::classList, TestSectionEntity::classList, ListSerializer(TestClassItem.serializer())),
                  //  SyncedSerialized(TestSection::metaTags, TestSectionEntity::metaTags, ListSerializer(TestMetaTag.serializer())),
                    ReadOnly(TestSection::pageId, TestSectionEntity::pageIdValue)
                )
            }
        }
    }
}