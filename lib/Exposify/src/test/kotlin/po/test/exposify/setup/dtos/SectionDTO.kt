package po.test.exposify.setup.dtos

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.delegates.binding
import po.exposify.dto.components.property_binder.delegates.foreign2IdReference
import po.exposify.dto.components.property_binder.delegates.parent2IdReference
import po.exposify.dto.components.property_binder.delegates.serializedBinding
import po.exposify.dto.components.relation_binder.delegates.oneToManyOf
import po.exposify.dto.enums.Cardinality
import po.exposify.scope.sequence.classes.SwitchHandlerProvider
import po.test.exposify.setup.ClassData
import po.test.exposify.setup.ContentBlockEntity
import po.test.exposify.setup.MetaData
import po.test.exposify.setup.SectionEntity

@Serializable
data class Section(
    override var id: Long = 0L,
    var name: String,
    var description: String,
    @SerialName("json_ld")
    var jsonLd: String,
    @SerialName("class_list")
    var classList: List<ClassData>,
    @SerialName("meta_tags")
    var metaTags: List<MetaData>,
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

    var classList:  List<ClassData> by serializedBinding(Section::classList, SectionEntity::classList)
    var metaTags :  List<MetaData> by serializedBinding(Section::metaTags, SectionEntity::metaTags)

    val updatedBy : Long by foreign2IdReference(Section::updatedBy, SectionEntity::updatedBy, UserDTO)
    val pageId : Long by parent2IdReference(Section::pageId, SectionEntity::page)

    val contentBlocks : List<ContentBlockDTO> by oneToManyOf(
        ContentBlockDTO,
        Section::contentBlocks,
        SectionEntity::contentBlocks,
        ContentBlockEntity::section)

    companion object: DTOClass<SectionDTO, Section, SectionEntity>(PageDTO){

       val UPDATE by SwitchHandlerProvider(this, Cardinality.ONE_TO_MANY, PageDTO.SELECT)
       val SELECT_UPDATE by SwitchHandlerProvider(this, Cardinality.ONE_TO_MANY, PageDTO.UPDATE)

        override fun setup() {
            configuration<SectionDTO, Section, SectionEntity>(SectionEntity) {
                applyTrackerConfig {
                    name = "AltSection"
                    observeProperties = true
                    observeRelationBindings = true
                }
            }
        }
    }
}