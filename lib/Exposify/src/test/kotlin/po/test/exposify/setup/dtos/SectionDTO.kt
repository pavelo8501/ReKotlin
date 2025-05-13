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
import po.exposify.dto.components.property_binder.delegates.binding
import po.exposify.dto.components.property_binder.delegates.foreign2IdReference
import po.exposify.dto.components.property_binder.delegates.parent2IdReference
import po.exposify.dto.components.property_binder.delegates.serializedBinding
import po.exposify.dto.components.relation_binder.delegates.oneToManyOf
import po.test.exposify.setup.ClassItem
import po.test.exposify.setup.ContentBlockEntity
import po.test.exposify.setup.MetaTag
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.SectionEntity
import po.test.exposify.setup.UserEntity

@Serializable
data class Section(
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
    @SerialName("content_blocks")
    val contentBlocks : MutableList<ContentBlock> = mutableListOf()

    var updated: LocalDateTime = UserDTO.nowTime()
}

class SectionDTO(
    override var dataModel: Section
): CommonDTO<SectionDTO, Section, SectionEntity>(SectionDTO) {

    var name : String by binding(Section::name, SectionEntity::name)
    var description : String by binding(Section::description, SectionEntity::description)
    var jsonLd : String by binding(Section::jsonLd, SectionEntity::jsonLd)
    var langId : Int by binding(Section::langId, SectionEntity::langId)
    var updated : LocalDateTime by binding(Section::updated, SectionEntity::updated)

    var classList:  List<ClassItem> by serializedBinding(Section::classList, SectionEntity::classList, ClassItem)
    var metaTags :  List<MetaTag> by serializedBinding(Section::metaTags, SectionEntity::metaTags, MetaTag)

    val updatedBy : Long by foreign2IdReference(Section::updatedBy, SectionEntity::updatedBy, UserEntity)
    val pageId : Long by parent2IdReference(Section::pageId, SectionEntity::page)

    val contentBlocks : List<ContentBlockDTO> by oneToManyOf(
        ContentBlockDTO,
        Section::contentBlocks,
        SectionEntity::contentBlocks,
        ContentBlockEntity::section)

    companion object: DTOClass<SectionDTO, Section, SectionEntity>(PageDTO){
        override suspend fun setup() {
            configuration<SectionDTO, Section, SectionEntity>(SectionEntity) {

            }
        }
    }
}