package po.test.exposify.setup.dtos

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.bindings.SerializedBinding
import po.exposify.dto.components.property_binder.bindings.SyncedBinding
import po.exposify.dto.components.property_binder.delegates.parent2IdReference
import po.test.exposify.setup.ClassItem
import po.test.exposify.setup.ContentBlockEntity
import po.test.exposify.setup.MetaTag


@Serializable
data class ContentBlock(
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
    @SerialName("section_id")
    var sectionId : Long,
): DataModel
{
    var updated: LocalDateTime = ContentBlockDTO.nowTime()
}


class ContentBlockDTO(
    override var dataModel: ContentBlock
): CommonDTO<ContentBlockDTO, ContentBlock, ContentBlockEntity>(ContentBlockDTO) {

    val sectionId by parent2IdReference(ContentBlock::sectionId, ContentBlockEntity::section)

    companion object: DTOClass<ContentBlockDTO, ContentBlock, ContentBlockEntity>(SectionDTO){
        override suspend  fun setup() {

            configuration<ContentBlockDTO, ContentBlock, ContentBlockEntity>(ContentBlockEntity){
                propertyBindings(
                    SyncedBinding(ContentBlock::name, ContentBlockEntity::name),
                    SyncedBinding(ContentBlock::content, ContentBlockEntity::content),
                    SyncedBinding(ContentBlock::tag, ContentBlockEntity::tag),
                    SyncedBinding(ContentBlock::jsonLd, ContentBlockEntity::jsonLd),
                    SyncedBinding(ContentBlock::updated, ContentBlockEntity::updated),
                    SyncedBinding(ContentBlock::langId, ContentBlockEntity::langId),

                    SerializedBinding(ContentBlock::classList, ContentBlockEntity::classList, ListSerializer(
                        ClassItem.serializer()) ),
                    SerializedBinding(ContentBlock::metaTags, ContentBlockEntity::metaTags, ListSerializer(
                        MetaTag.serializer())),
                )
            }
        }
    }
}
