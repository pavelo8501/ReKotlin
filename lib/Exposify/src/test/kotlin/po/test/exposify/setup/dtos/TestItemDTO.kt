package po.test.exposify.setup.dtos

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.CommonDTO
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.property_binder.delegates.binding
import po.exposify.dto.components.bindings.property_binder.delegates.nullableBinding
import po.exposify.dto.configuration.configuration
import po.exposify.dto.helpers.dtoOf
import po.exposify.dto.interfaces.DataModel
import po.misc.context.CTXIdentity
import po.misc.context.Identifiable
import po.misc.context.asIdentity


object TestsItems : LongIdTable("tests_items", "id") {
    val name = varchar("name",128)
    val nullable = text("nullable").nullable()
}

class TestsItemEntity (id: EntityID<Long>) : LongEntity(id){
    companion object : ExposifyEntityClass<TestsItemEntity>(TestsItems)
    var name by TestsItems.name
    var nullable by TestsItems.nullable
}


@Serializable
data class TestItem(
    override var id: Long = 0,
    var name: String,
    var nullable: String?
): DataModel{

}


class TestItemDTO(): CommonDTO<TestItemDTO, TestItem, TestsItemEntity>(TestItemDTO) {

    var name: String by binding(TestItem::name, TestsItemEntity::name)

    var nullable : String? by nullableBinding(TestItem::nullable, TestsItemEntity::nullable)

    companion object: RootDTO<TestItemDTO, TestItem, TestsItemEntity>(dtoOf(TestItemDTO)){
        override fun setup() {
            configuration{ }
        }
    }
}