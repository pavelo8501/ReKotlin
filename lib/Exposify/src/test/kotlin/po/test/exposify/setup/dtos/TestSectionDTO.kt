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
import po.exposify.dto.components.property_binder.delegates.idReferenced
import po.test.exposify.setup.TestClassItem
import po.test.exposify.setup.TestPageEntity
import po.test.exposify.setup.TestSectionEntity

@Serializable
data class TestSection(
    override var id: Long,
    var name: String,
    var description: String,
    @SerialName("json_ld")
    var jsonLd: String,
    @SerialName("class_list")
    var classList: List<TestClassItem>,
    @SerialName("lang_id")
    var langId: Int,
    @SerialName("updated_by")
    var updatedBy: Long,
    @SerialName("page_id")
    var pageId : Long  ): DataModel
{
    var updated: LocalDateTime = TestUserDTO.nowTime()
}

class TestSectionDTO(
    override var dataModel: TestSection
): CommonDTO<TestSectionDTO, TestSection, TestSectionEntity>(TestSectionDTO) {

    val updatedById : Long by idReferenced(TestSection::updatedBy, TestSectionEntity::updatedBy, TestUserDTO)

   // val pageDto by parentReference(TestSection::pageId, TestSectionEntity::page, TestSectionDTO)

    companion object: DTOClass<TestSectionDTO>(){
        override suspend fun setup() {
            configuration<TestSectionDTO, TestSection, TestSectionEntity>(TestSectionEntity) {
                propertyBindings(
                    SyncedBinding(TestSection::name, TestSectionEntity::name),
                    SyncedBinding(TestSection::description, TestSectionEntity::description),
                    SyncedBinding(TestSection::jsonLd, TestSectionEntity::jsonLd),
                    SyncedBinding(TestSection::langId, TestSectionEntity::langId ),
                    SyncedBinding(TestSection::updated, TestSectionEntity::updated),
                    SerializedBinding(TestSection::classList, TestSectionEntity::classList, ListSerializer(TestClassItem.serializer())),
                )
                childBindings {
                }
            }
        }
    }
}