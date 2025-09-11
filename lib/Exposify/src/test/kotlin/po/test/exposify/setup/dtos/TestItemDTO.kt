package po.test.exposify.setup.dtos

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import po.exposify.dto.CommonDTO
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.property_binder.delegates.binding
import po.exposify.dto.configuration.configuration
import po.exposify.dto.helpers.dtoOf
import po.exposify.dto.interfaces.DataModel
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.Identifiable
import po.misc.context.asIdentity
import po.test.exposify.setup.TestsItemEntity

@Serializable
data class TestItem(
    override var id: Long = 0,
    var name: String
): DataModel, Identifiable<TestItem>{

    @Contextual
    override val identity: CTXIdentity<TestItem> = asIdentity()

}


class TestItemDTO(): CommonDTO<TestItemDTO, TestItem, TestsItemEntity>(TestItemDTO) {

    var name : String by binding(TestItem::name, TestsItemEntity::name)

    companion object: RootDTO<TestItemDTO, TestItem, TestsItemEntity>(dtoOf(TestItemDTO)){
        override fun setup() {
            configuration{ }
        }
    }
}