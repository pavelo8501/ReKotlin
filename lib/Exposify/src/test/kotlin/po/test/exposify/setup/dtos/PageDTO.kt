package po.test.exposify.setup.dtos

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import po.exposify.classes.RootDTO
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.bindings.SyncedBinding
import po.exposify.dto.components.property_binder.delegates.foreign2IdReference
import po.exposify.dto.components.relation_binder.delegates.oneToManyOf
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.SectionEntity
import po.test.exposify.setup.UserEntity


@Serializable
data class Page(
    override var id: Long = 0,
    var name: String,
    @SerialName("lang_id")
    var langId: Int,
    @SerialName("updated_by")
    var updatedById: Long
): DataModel
{
    var updated: LocalDateTime = PageDTO.nowTime()
    var sections: MutableList<Section> = mutableListOf()
}

class PageDTO(
    override var dataModel: Page
): CommonDTO<PageDTO, Page, PageEntity>(PageDTO) {

    val updatedById by foreign2IdReference(Page::updatedById, PageEntity::updatedBy, UserEntity)

    val sections by oneToManyOf(
        childClass = SectionDTO,
        ownDataModels =  Page::sections,
        ownEntities =  PageEntity::sections,
        foreignEntity = SectionEntity::page)


    companion object: RootDTO<PageDTO, Page>(){
        override suspend fun setup() {
            configuration<PageDTO, Page, PageEntity>(PageEntity){
                propertyBindings(
                    SyncedBinding(Page::name, PageEntity::name),
                    SyncedBinding(Page::langId,  PageEntity::langId),
                    SyncedBinding(Page::updated, PageEntity::updated),
                )
                hierarchyMembers(SectionDTO, ContentBlockDTO)
            }
        }
    }
}