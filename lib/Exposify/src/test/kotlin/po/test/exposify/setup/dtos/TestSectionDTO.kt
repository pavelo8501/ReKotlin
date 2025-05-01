package po.test.exposify.setup.dtos

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.bindings.SerializedBinding
import po.exposify.dto.components.property_binder.bindings.SyncedBinding
import po.exposify.dto.components.property_binder.delegates.foreign2IdReference
import po.exposify.dto.components.relation_binder.delegates.oneToManyOf
import po.test.exposify.setup.ClassItem
import po.test.exposify.setup.MetaTag
import po.test.exposify.setup.TestPageEntity
import po.test.exposify.setup.TestSectionEntity
import po.test.exposify.setup.TestSectionItemEntity
import po.test.exposify.setup.TestUserEntity

@Serializable
data class TestSection(
    override var id: Long,
    var name: String,
    var description: String,
    @SerialName("json_ld")
    var jsonLd: String,
    @SerialName("class_list")
    var classList: List<ClassItem>,
    @SerialName("meta_tags")
    var metaTags: List<MetaTag>,
    @SerialName("lang_id")
    var langId: Int,
    @SerialName("updated_by")
    var updatedBy: Long,
    @SerialName("page_id")
    var pageId : Long,
   // var page: TestPage
): DataModel
{
    @SerialName("section_items")
    var sectionItems : MutableList<TestSectionItem> = mutableListOf()
    var updated: LocalDateTime = TestUserDTO.nowTime()
}

class TestSectionDTO(
    override var dataModel: TestSection
): CommonDTO<TestSectionDTO, TestSection, TestSectionEntity>(TestSectionDTO) {

    val updatedBy : Long by foreign2IdReference(TestSection::updatedBy, TestSectionEntity::updatedBy, TestUserEntity)
    val sectionItems by oneToManyOf(
           childClass =  TestSectionDTO,
           ownDataModels = TestSection::sectionItems,
           ownEntities =   TestSectionEntity::sectionItems,
           foreignEntity = TestSectionItemEntity::section)


   // val pageId : Long by parent2IdReference(TestSection::pageId, TestSectionEntity::page)
   // val page  by parentReference(TestSection::page, TestPageDTO, TestPageEntity)

    companion object: DTOClass<TestSectionDTO>(){
        override suspend fun setup() {
            configuration<TestSectionDTO, TestSection, TestSectionEntity>(TestSectionEntity) {
                propertyBindings(
                    SyncedBinding(TestSection::name, TestSectionEntity::name),
                    SyncedBinding(TestSection::description, TestSectionEntity::description),
                    SyncedBinding(TestSection::jsonLd, TestSectionEntity::jsonLd),
                    SyncedBinding(TestSection::langId, TestSectionEntity::langId ),
                    SyncedBinding(TestSection::updated, TestSectionEntity::updated),
                    SerializedBinding(TestSection::classList, TestSectionEntity::classList, ListSerializer(ClassItem.serializer())),
                    SerializedBinding(TestSection::metaTags, TestSectionEntity::metaTags, ListSerializer(MetaTag.serializer())),
                )
            }
        }
    }
}