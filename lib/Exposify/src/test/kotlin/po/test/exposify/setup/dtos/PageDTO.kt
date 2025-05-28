package po.test.exposify.setup.dtos

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.delegates.binding
import po.exposify.dto.components.property_binder.delegates.foreign2IdReference
import po.exposify.dto.components.relation_binder.delegates.oneToManyOf
import po.exposify.dto.configuration
import po.exposify.scope.sequence.classes.RootHandlerProvider
import po.test.exposify.setup.PageEntity


@Serializable
data class Page(
    override var id: Long = 0L,
    var name: String = "",
    @SerialName("lang_id")
    var langId: Int = 1,
    @SerialName("updated_by")
    var updatedById: Long = 0
): DataModel
{
    var updated: LocalDateTime = PageDTO.nowTime()
    var sections: MutableList<Section> = mutableListOf()
}

class PageDTO(
    override var dataModel: Page
): CommonDTO<PageDTO, Page, PageEntity>(this) {

    var name : String by binding(Page::name, PageEntity::name)
    var langId : Int by binding(Page::langId, PageEntity::langId)
    var updated : LocalDateTime by binding(Page::updated, PageEntity::updated)

    val updatedById : Long by foreign2IdReference(Page::updatedById, PageEntity::updatedBy, UserDTO)
    val sections : List<SectionDTO> by oneToManyOf(SectionDTO, Page::sections, PageEntity::sections)

    //val sections: List<SectionDTO> by oneToManyOfAdv(SectionDTO, Page::sections, PageEntity::sections)

    companion object: RootDTO<PageDTO, Page, PageEntity>(){

        val UPDATE by RootHandlerProvider(this)
        val SELECT by RootHandlerProvider(this)

        override fun setup() {

            configuration{
                applyTrackerConfig {
                    name = "page"
                    observeProperties = true
                    observeRelationBindings = true
                }
                hierarchyMembers(SectionDTO, ContentBlockDTO)
                useDataModelBuilder { Page() }
            }
        }

    }
}