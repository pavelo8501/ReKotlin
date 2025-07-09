package po.test.exposify.setup.dtos

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.bindings.property_binder.delegates.attachedReference
import po.exposify.dto.components.bindings.property_binder.delegates.binding
import po.exposify.dto.components.bindings.relation_binder.delegates.oneToManyOf
import po.exposify.dto.components.tracker.models.TrackerTag
import po.exposify.dto.configuration.configuration
import po.exposify.scope.sequence.builder.InsertSingle
import po.exposify.scope.sequence.builder.PickById
import po.exposify.scope.sequence.classes.RootHandlerProvider
import po.exposify.scope.sequence.launcher.IdLaunchDescriptor
import po.exposify.scope.sequence.launcher.ParametrizedSinge
import po.test.exposify.setup.PageEntity
import po.test.exposify.setup.SectionEntity

@Serializable
data class Page(
    override var id: Long = 0L,
    var name: String = "",
    @SerialName("lang_id")
    var langId: Int = 1,
    @SerialName("updated_by")
    var updatedBy: Long = 0
): DataModel
{
    var updated: LocalDateTime = PageDTO.nowTime()
    var sections: MutableList<Section> = mutableListOf()
}

class PageDTO(): CommonDTO<PageDTO, Page, PageEntity>(this){

    var name : String by binding(Page::name, PageEntity::name)
    var langId : Int by binding(Page::langId, PageEntity::langId)
    var updated : LocalDateTime by binding(Page::updated, PageEntity::updated)

    var updatedBy: Long = 0

    val user by attachedReference(UserDTO, Page::updatedBy, PageEntity::updatedBy){user->
        updatedBy = user.id
    }

    val sections : List<SectionDTO> by oneToManyOf(SectionDTO, Page::sections, PageEntity::sections, SectionEntity::page)

    companion object: RootDTO<PageDTO, Page, PageEntity>(PageDTO::class){

        val INSERT = ParametrizedSinge(this, InsertSingle)
        val PICK = IdLaunchDescriptor(this, PickById)

        val UPDATE by RootHandlerProvider(this)
        val SELECT by RootHandlerProvider(this)

        //val launcher: LaunchConfigurator<PageDTO, ResultSingle<PageDTO,*,*>> = launch(this)

        override fun setup() {

            configuration{
                applyTrackerConfig {
                    aliasName = "page"
                    observeProperties = true
                    observeRelationBindings = true
                    trackerTag = TrackerTag.BreakPoint
                }
            }
        }

    }
}