package po.test.exposify.structure

import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import po.exposify.dto.interfaces.DataModel
import po.exposify.entity.classes.ExposifyEntity


@kotlinx.serialization.Serializable
data class RootData(override var id: Long = 0, var name: String = "", var children: MutableList<ChildData> = mutableListOf()) : DataModel

@kotlinx.serialization.Serializable
data class ChildData(override var id: Long = 0, var value: String = "", var rootId: Long = 0) : DataModel

object RootEntities : LongIdTable("test_root") {
    val name = varchar("name", 128)
}

object ChildEntities : LongIdTable("test_child") {
    val value = varchar("value", 128)
    val root = reference("root_id", RootEntities)
}

class RootEntity(id: EntityID<Long>) : ExposifyEntity(id) {
    companion object : LongEntityClass<RootEntity>(RootEntities)
    var name by RootEntities.name
    val children by ChildEntity referrersOn ChildEntities.root
}

class ChildEntity(id: EntityID<Long>) : ExposifyEntity(id) {
    companion object : LongEntityClass<ChildEntity>(ChildEntities)
    var value by ChildEntities.value
    var root by RootEntity referencedOn ChildEntities.root
}




//class RootDTO(var dataModel: RootData)
//    : CommonDTO<RootDTO, RootData, RootEntity>(RootDTO) {
//
//    val children by oneToManyOf(
//        childClass = ChildDTO,
//        ownDataModels = RootData::children,
//        ownEntities = RootEntity::children,
//        foreignEntity = ChildEntity::root
//    )
//
//    companion object : RootDTO<RootDTO, RootData>() {
//        override suspend fun setup() {
//            configuration<RootDTO, RootData, RootEntity>(RootEntity) {
//                propertyBindings(SyncedBinding(RootData::name, RootEntity::name))
//            }
//        }
//    }
//}
//
//class ChildDTO(override var dataModel: ChildData)
//    : CommonDTO<ChildDTO, ChildData, ChildEntity>(ChildDTO) {
//
//    companion object : DTOClass<ChildDTO>(RootDTO) {
//        override suspend fun setup() {
//            configuration<ChildDTO, ChildData, ChildEntity>(ChildEntity) {
//                propertyBindings(SyncedBinding(ChildData::value, ChildEntity::value))
//            }
//        }
//    }
//}
