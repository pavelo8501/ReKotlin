package po.test.exposify.setup.dtos

import kotlinx.serialization.Serializable
import po.exposify.dto.CommonDTO
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.property_binder.delegates.binding
import po.exposify.dto.configuration.configuration
import po.exposify.dto.interfaces.DataModel
import po.test.exposify.setup.TestsItemEntity

@Serializable
data class TestItem(
    override var id: Long = 0,
    var name: String,
): DataModel


class TestItemDTO(
    override var dataModel: TestItem
): CommonDTO<TestItemDTO, TestItem, TestsItemEntity>(TestItemDTO) {

    val name : String by binding(TestItem::name, TestsItemEntity::name)

    companion object: RootDTO<TestItemDTO, TestItem, TestsItemEntity>(TestItemDTO::class){
        override fun setup() {
            configuration{ }
        }
    }
}