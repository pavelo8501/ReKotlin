package po.test.exposify.structure

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import po.exposify.dto.interfaces.DataModel
import kotlinx.serialization.Serializable
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.property_binder.DTOPropertyBinder
import po.exposify.dto.components.property_binder.PropertyBinder
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTORegistryItem
import po.exposify.dto.models.DTOTracker
import po.exposify.entity.classes.ExposifyEntityClass
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.castOrOperationsEx
import po.lognotify.TasksManaged
import po.lognotify.extensions.startTaskAsync
import po.test.exposify.structure.ClassDTOTest
import kotlin.reflect.KClass

//
//@Serializable
//data class TopData(override var id: Long = 0, var name: String = "", var children: MutableList<ChildData> = mutableListOf()) : DataModel
//
//@Serializable
//data class ChildData(override var id: Long = 0, var value: String = "", var rootId: Long = 0) : DataModel
//
//object TopEntities : LongIdTable("test_root") {
//    val name = varchar("name", 128)
//}
//
//object ChildEntities : LongIdTable("test_child") {
//    val value = varchar("value", 128)
//    val root = reference("root_id", TopEntities)
//}
//
//class RootEntity(id: EntityID<Long>) : ExposifyEntity(id) {
//    companion object : ExposifyEntityClass<RootEntity>(TopEntities)
//    var name by TopEntities.name
//    val children by ChildEntity referrersOn ChildEntities.root
//}
//
//class ChildEntity(id: EntityID<Long>) : ExposifyEntity(id) {
//    companion object : LongEntityClass<ChildEntity>(ChildEntities)
//    var value by ChildEntities.value
//    var root by RootEntity referencedOn ChildEntities.root
//}
//
//abstract class CommonDTOTest<DATA, ENTITY>(
//    val dtoClass: RootDTOTest<*, DATA, ENTITY>
//): ModelDTO where DATA: DataModel , ENTITY: ExposifyEntity {
//
//    lateinit var dtoClassConfig: DTOConfigTest<DATA, ENTITY>
//    private lateinit var registryItem : CommonDTORegistryItem<ModelDTO, DATA, ENTITY>
//    override val dtoName : String get() = "[CommonDTO ${registryItem.commonDTOKClass.simpleName.toString()}]"
//    abstract override val dataModel: DATA
//    override lateinit var daoService: DAOService<ModelDTO, DATA, ENTITY>
//    private var insertedEntity : ENTITY? = null
//    val daoEntity : ENTITY
//        get() {
//            return  insertedEntity?:daoService.entityModel[id]
//        }
//    internal  lateinit var dtoPropertyBinder : DTOPropertyBinder<ModelDTO, DATA, ENTITY>
//    override lateinit var propertyBinder : PropertyBinder<DATA, ENTITY>
//
//    override lateinit var dataContainer: DataModelContainer<*, *>
//    override lateinit var dtoTracker: DTOTracker<*, *>
//    override var id: Long = 0L
//}
//
//class DTOConfigTest<DATA, ENTITY>(
//    val entityModel:LongEntityClass<ENTITY>,
//    val dtoClass : RootDTOTest<* ,DATA, ENTITY>
//) where  DATA: DataModel,  ENTITY : ExposifyEntity{
//
//}
//
//interface ClassDTOTest{
//
//    fun nowTime(): LocalDateTime {
//        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
//    }
//}
//
//abstract class RootDTOTest<DTO,  DATA, ENTITY>()
//    :TasksManaged,  ClassDTOTest
//        where DATA: DataModel, ENTITY: ExposifyEntity, DTO : CommonDTOTest<DATA, ENTITY>{
//
//   lateinit var config : DTOConfigTest<DATA, ENTITY>
//
//    internal var initialConfig: DTOConfigTest<DATA, ENTITY>? = null
//    protected abstract suspend fun  setup()
//
//    inline fun <reified COMMON,  reified DATA, reified ENTITY> configuration(
//        entityModel: LongEntityClass<ENTITY>,
//        noinline block: suspend DTOConfigTest<DATA, ENTITY>.() -> Unit
//    ): Unit where COMMON : CommonDTOTest<DATA, ENTITY>,  ENTITY : ExposifyEntity, DATA : DataModel = startTaskAsync("DTO Configuration") {
//        val commonDTOClass =  COMMON::class.castOrInitEx<KClass<out CommonDTOTest<DATA, ENTITY>>>(
//            "KClass<out CommonDTO<DTO, DATA, ENTITY> cast failed")
//    }.resultOrException()
//}
//
//
//
//class TopDTO(override var dataModel: TopData)
//   : CommonDTOTest<TopData, RootEntity>(TopDTO) {
//
//    companion object: RootDTOTest<TopDTO, TopData, RootEntity>(){
//        override suspend fun setup() {
//            configuration<TopDTO, TopData, RootEntity>(RootEntity){
//
//            }
//        }
//    }
//}