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
import po.exposify.dto.components.property_binder.delegates.parentReference
import po.test.exposify.setup.TestClassItem
import po.test.exposify.setup.TestPageEntity
import po.test.exposify.setup.TestSectionEntity
import kotlin.reflect.KMutableProperty1


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

    @SerialName("page_id")
    var pageId : Long = 0L
    var updated: LocalDateTime = TestUserDTO.nowTime()
}

class TestSectionDTO(
    override var dataModel: TestSection
): CommonDTO<TestSectionDTO, TestSection, TestSectionEntity>(TestSectionDTO) {

    var dataPageId by parentReference<TestSection, TestPageEntity>{
        dataModelProperty(TestSection::pageId)
        referencedEntityModel(TestPageEntity)
    }

    companion object: DTOClass<TestSectionDTO>(){
        override suspend fun setup() {
            configuration<TestSectionDTO, TestSection, TestSectionEntity>(TestSectionEntity) {
                propertyBindings(
                    SyncedBinding(TestSection::name, TestSectionEntity::name),
                    SyncedBinding(TestSection::description, TestSectionEntity::description),
                    SyncedBinding(TestSection::jsonLd, TestSectionEntity::jsonLd),
                    SyncedBinding(TestSection::updated, TestSectionEntity::updated),
                    SyncedBinding(TestSection::langId, TestSectionEntity::langId ),
                    SerializedBinding(TestSection::sectionClasses, TestSectionEntity::sectionClasses, ListSerializer(TestClassItem.serializer())),
                   // ReferencedBindingDepr(TestSection::updatedById, TestSectionEntity::updatedBy, TestUserDTO),
                )
                childBindings {
                }
            }
        }
    }
}