package po.test.exposify.setup.dtos

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.bindings.property_binder.delegates.binding
import po.exposify.dto.components.bindings.property_binder.delegates.serializedBinding
import po.exposify.dto.components.bindings.relation_binder.delegates.parentReference
import po.exposify.dto.configuration.configuration
import po.exposify.dto.helpers.dtoOf
import po.exposify.dto.interfaces.DataModel
import po.test.exposify.setup.ClassData
import po.test.exposify.setup.ContentBlockEntity
import po.test.exposify.setup.MetaData
import po.test.exposify.setup.SectionEntity

@Serializable
data class ContentBlock(
    override var id: Long = 0L,
    var name: String,
    var content: String,
    var tag: String,
    @SerialName("json_ld")
    var jsonLd: String,
    @SerialName("class_list")
    var classList: List<ClassData>,
    @SerialName("meta_tags")
    var metaTags: List<MetaData>,
    @SerialName("lang_id")
    var langId: Int,
    @SerialName("section_id")
    var sectionId: Long,
) : DataModel {
    var updated: LocalDateTime = ContentBlockDTO.nowTime()
}

class ContentBlockDTO : CommonDTO<ContentBlockDTO, ContentBlock, ContentBlockEntity>(ContentBlockDTO) {
    var name: String by binding(ContentBlock::name, ContentBlockEntity::name)
    var content: String by binding(ContentBlock::content, ContentBlockEntity::content)
    var tag: String by binding(ContentBlock::tag, ContentBlockEntity::tag)
    var jsonLd: String by binding(ContentBlock::jsonLd, ContentBlockEntity::jsonLd)
    var langId: Int by binding(ContentBlock::langId, ContentBlockEntity::langId)
    var updated: LocalDateTime by binding(ContentBlock::updated, ContentBlockEntity::updated)

    var classList: List<ClassData> by serializedBinding(ContentBlock::classList, ContentBlockEntity::classList)
    var metaTags: List<MetaData> by serializedBinding(ContentBlock::metaTags, ContentBlockEntity::metaTags)

    val section: SectionDTO by parentReference(SectionDTO, ContentBlockEntity::section) { section ->
        sectionId = section.id
    }

    companion object : DTOClass<ContentBlockDTO, ContentBlock, ContentBlockEntity>(dtoOf(ContentBlockDTO), SectionDTO) {
        override fun setup() {
            configuration {
            }
        }
    }
}
