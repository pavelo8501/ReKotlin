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
import po.exposify.dto.components.property_binder.delegates.parent2IdReference
import po.test.exposify.setup.ClassItem
import po.test.exposify.setup.MetaTag
import po.test.exposify.setup.TestSectionItemEntity


@Serializable
data class TestSectionItem(
    override var id: Long,
    var name: String,
    var content: String,
    var tag: String,
    @SerialName("json_ld")
    var jsonLd : String,
    @SerialName("class_list")
    var classList: List<ClassItem>,
    @SerialName("meta_tags")
    var metaTags : List<MetaTag>,
    @SerialName("lang_id")
    var langId : Int,
    @SerialName("section_item_id")
    var sectionItemId : Long,
): DataModel
{

    var updated: LocalDateTime = TestSectionItemDTO.nowTime()
}


class TestSectionItemDTO(
    override var dataModel: TestSectionItem
): CommonDTO<TestSectionItemDTO, TestSectionItem, TestSectionItemEntity>(TestSectionItemDTO) {

    val sectionItemId by parent2IdReference(TestSectionItem::sectionItemId, TestSectionItemEntity::section)

    companion object: DTOClass<TestSectionItemDTO>(){
        override suspend  fun setup() {

            configuration<TestSectionItemDTO, TestSectionItem, TestSectionItemEntity>(TestSectionItemEntity){
                propertyBindings(
                    SyncedBinding(TestSectionItem::name, TestSectionItemEntity::name),
                    SyncedBinding(TestSectionItem::content, TestSectionItemEntity::content),
                    SyncedBinding(TestSectionItem::tag, TestSectionItemEntity::tag),
                    SyncedBinding(TestSectionItem::jsonLd, TestSectionItemEntity::jsonLd),
                    SyncedBinding(TestSectionItem::updated, TestSectionItemEntity::updated),
                    SyncedBinding(TestSectionItem::langId, TestSectionItemEntity::langId),

                    SerializedBinding(TestSectionItem::classList, TestSectionItemEntity::classList, ListSerializer(
                        ClassItem.serializer()) ),
                    SerializedBinding(TestSectionItem::metaTags, TestSectionItemEntity::metaTags, ListSerializer(
                        MetaTag.serializer())),
                )
            }
        }
    }
}
