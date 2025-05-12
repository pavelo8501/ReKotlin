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
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.property_binder.DTOPropertyBinder
import po.exposify.dto.components.property_binder.PropertyBinder
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTORegistryItem
import po.exposify.dto.models.DTOTracker
import po.exposify.entity.classes.ExposifyEntityClass
import po.lognotify.TasksManaged
import po.lognotify.extensions.newTaskAsync
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty



    @Serializable
    data class TopData(
        override var id: Long = 0,
        var name: String = "",
        var children: MutableList<ChildData> = mutableListOf()

    ) : DataModel

    @Serializable
    data class ChildData(override var id: Long = 0, var value: String = "", var rootId: Long = 0, var parent: TopData) : DataModel{



    }

    object TopEntities : LongIdTable("test_root") {
        val name = varchar("name", 128)

    }

    object ChildEntities : LongIdTable("test_child") {
        val value = varchar("value", 128)
        val root = reference("root_id", TopEntities)
    }

    class RootEntity(id: EntityID<Long>) : LongEntity(id) {
        companion object : ExposifyEntityClass<RootEntity>(TopEntities)

        var name by TopEntities.name
        val children by ChildEntity referrersOn ChildEntities.root
    }

    class ChildEntity(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<ChildEntity>(ChildEntities)

        var value by ChildEntities.value
        var root by RootEntity referencedOn ChildEntities.root
    }


    abstract class CommonDTOTest<DTO, DATA, ENTITY>(
        val dtoClass: BaseDTO<DATA, ENTITY>,
        val dtoClass2: () -> BaseDTO<DATA, ENTITY>) : ModelDTO where  DATA : DataModel, ENTITY : LongEntity, DTO: ModelDTO
    {

        lateinit var dtoClassConfig: DTOConfigTest<DATA, ENTITY>
        private lateinit var registryItem: CommonDTORegistryItem<ModelDTO, DATA, ENTITY>
        override val dtoName: String get() = "[CommonDTO ${registryItem.commonDTOKClass.simpleName.toString()}]"
        abstract override val dataModel: DATA
        override lateinit var daoService: DAOService<ModelDTO, DATA, ENTITY>
        private var insertedEntity: ENTITY? = null
        val daoEntity: ENTITY
            get() {
                return insertedEntity ?: daoService.entityModel[id]
            }
        internal lateinit var dtoPropertyBinder: DTOPropertyBinder<DTO, DATA, ENTITY>
        override lateinit var propertyBinder: PropertyBinder<DATA, ENTITY>

        override lateinit var dataContainer: DataModelContainer<*, *>
        override lateinit var dtoTracker: DTOTracker<*, *>
        override var id: Long = 0L
    }

    class DTOConfigTest<DATA, ENTITY>(
        val entityModel: LongEntityClass<ENTITY>,
        val dtoClass: RootDTOTest<DATA, ENTITY>
    ) where  DATA : DataModel, ENTITY : LongEntity {
    }

    interface ClassDTOTest {

        fun nowTime(): LocalDateTime {
            return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
        }
    }

    abstract class RootDTOTest<DATA, ENTITY>() :BaseDTO<DATA, ENTITY>(), ClassDTOTest, TasksManaged
            where DATA : DataModel, ENTITY : LongEntity{

        lateinit var config: DTOConfigTest<DATA, ENTITY>

        internal var initialConfig: DTOConfigTest<DATA, ENTITY>? = null
        protected abstract suspend fun setup()

        inline fun <reified COMMON, reified DATA, reified ENTITY> configuration(
            entityModel: LongEntityClass<ENTITY>,
            noinline block: suspend DTOConfigTest<DATA, ENTITY>.() -> Unit
        ): Unit where COMMON : CommonDTOTest<COMMON, DATA, ENTITY>, ENTITY : LongEntity, DATA : DataModel =
            newTaskAsync("DTO Configuration", "RootDTOTest") {

            }.resultOrException()
    }

    sealed class BaseDTO<DATA, ENTITY>()
            where DATA : DataModel, ENTITY : LongEntity


    fun <DTO, DATA,  ENTITY,F_DTO, FD, FE>  CommonDTOTest<DTO, DATA, ENTITY>.parentReference(
        parentDtoClass: BaseDTO<FD, FE>,
        dataProperty : KMutableProperty1<DATA, FD>,
        parentEntityModel: ExposifyEntityClass<FE>
    ): ParentDelegate<DTO, DATA, ENTITY,F_DTO, FD, FE>
            where  DTO: ModelDTO, DATA:DataModel, ENTITY : LongEntity,
                   FD: DataModel, FE: LongEntity, F_DTO: ModelDTO
    {
        val container = ParentDelegate<DTO, DATA, ENTITY ,F_DTO,  FD, FE>(this, parentEntityModel)

        return container
    }

    class ParentDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>(
        dto: CommonDTOTest<DTO, DATA, ENTITY>,
        private val entityModel: ExposifyEntityClass<FE>
    ): ReadOnlyProperty<DTO,  BaseDTO<FD, FE> >
            where DATA: DataModel, ENTITY: LongEntity, DTO : ModelDTO,
                  FD : DataModel, FE: LongEntity, F_DTO: ModelDTO
    {
        override fun getValue(
            thisRef: DTO,
            property: KProperty<*>
        ):  BaseDTO<FD, FE> {
            TODO("Not yet implemented")
        }
    }


class ChildDTO(override var dataModel: ChildData) : CommonDTOTest<ChildDTO ,ChildData, ChildEntity>(ChildDTO, {ChildDTO}) {

   // val parentDTO  by parentReference(TopDTO, ChildData::parent, RootEntity)

    companion object : RootDTOTest<ChildData, ChildEntity>() {
        override suspend fun setup() {

        }
    }
}

    class TopDTO(override var dataModel: TopData) : CommonDTOTest<TopDTO, TopData, RootEntity>(TopDTO, {TopDTO}) {
        companion object : RootDTOTest<TopData, RootEntity>() {
            override suspend fun setup() {

            }
        }
    }