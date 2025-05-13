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
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.SectionEntity
import po.test.exposify.setup.UserEntity


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
): CommonDTO<PageDTO, Page, PageEntity>(PageDTO) {

    var name : String by binding(Page::name, PageEntity::name)
    var langId : Int by binding(Page::langId, PageEntity::langId)
    var updated : LocalDateTime by binding(Page::updated, PageEntity::updated)

    val updatedById : Long by foreign2IdReference(Page::updatedById, PageEntity::updatedBy, UserEntity)
    val sections : List<SectionDTO> by oneToManyOf(SectionDTO, Page::sections, PageEntity::sections, SectionEntity::page)

    companion object: RootDTO<PageDTO, Page, PageEntity>(){
        override suspend fun setup() {
            configuration<PageDTO, Page, PageEntity>(PageEntity){
                hierarchyMembers(SectionDTO, ContentBlockDTO)
                useDataModelBuilder { Page() }
            }
        }
    }
}