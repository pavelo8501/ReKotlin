package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.DatabaseManager
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.components.tracker.models.TrackerConfig
import po.exposify.dto.models.CommonDTOType
import po.exposify.scope.connection.ConnectionClass
import po.lognotify.TasksManaged
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.processors.SeverityLevel
import po.misc.serialization.SerializerInfo
import po.misc.types.toSimpleNormalizedKey
import kotlin.reflect.KType


interface DTOConfiguration<DTO: ModelDTO, D: DataModel,  E : LongEntity> {

    fun <F: ModelDTO, FD: DataModel, FE: LongEntity> addHierarchMember(
        parent: DTOBase<DTO, D, E>,
        childMember: DTOClass<F, FD, FE>
    ):DTOClass<F, FD, FE>

    fun serializerLookup(type: KType): SerializerInfo<*>?
    fun registerDTO(dto: CommonDTO<DTO, D, E>)
    fun clearCachedDTOs()
    fun lookupDTO():List<CommonDTO<DTO, D, E>>
    fun lookupDTO(id: Long): CommonDTO<DTO, D, E>?
}


class DTOConfig<DTO, D, E>(
    val commonDTOType:CommonDTOType<DTO, D, E>
):DTOConfiguration<DTO, D, E>, TasksManaged where DTO: ModelDTO, D: DataModel, E : LongEntity {

    override val identity: CTXIdentity<DTOConfig<DTO, D, E>> = asIdentity()

    @PublishedApi
    internal val dtoFactory: DTOFactory<DTO, D, E> = DTOFactory(this)
    internal val daoService: DAOService<DTO, D, E> = DAOService(commonDTOType)

    internal var trackerConfigModified: Boolean = false
    val trackerConfig: TrackerConfig<*> = TrackerConfig()
    val childClasses: MutableMap<CommonDTOType<*, *, *>, DTOClass<*, *, *>> = mutableMapOf()
    val connectionClass: ConnectionClass? get() = DatabaseManager.connections.firstOrNull()

    val dtoMap : MutableMap<Long, CommonDTO<DTO, D, E>> = mutableMapOf()
    val dtoMapSize: Int get() = dtoMap.size

    override fun serializerLookup(type: KType): SerializerInfo<*>? {
        val normalizedKey = type.toSimpleNormalizedKey()
        val connection = connectionClass
        if (connection != null) {
            return connection.serializerMap[normalizedKey]
        }
        return null
    }

    override fun <F : ModelDTO, FD : DataModel, FE : LongEntity> addHierarchMember(
        parent: DTOBase<DTO, D, E>,
        childMember: DTOClass<F, FD, FE>
    ): DTOClass<F, FD, FE> {

        if (childClasses.contains(childMember.commonDTOType)) {
            notify("childClasses map contains key ${childMember.commonDTOType}", SeverityLevel.WARNING)
        } else {
            parent.onNewMember.trigger(DTOBase.Events.NewHierarchyMember, childMember)
            childClasses.put(childMember.commonDTOType, childMember)
        }
        return childMember
    }

    override fun registerDTO(dto: CommonDTO<DTO, D, E>){
        val usedId = if(dto.id < 1){
            dto.dtoId.id
        }else{
            dto.id
        }
        val exists = dtoMap.containsKey(usedId)
        if(exists){
            notify("Given dto with id: ${dto.id} already exist in dtoMap", SeverityLevel.WARNING)
        }
        dtoMap.putIfAbsent(usedId, dto)
    }

    override fun clearCachedDTOs(){
        dtoMap.clear()
    }

    override fun lookupDTO(id: Long): CommonDTO<DTO, D, E>?{
        return dtoMap[id]?:run {
            dtoMap.values.firstOrNull {it.id == id}
        }
    }
    override fun lookupDTO(): List<CommonDTO<DTO, D, E>>{
        return dtoMap.values.toList()
    }


    fun applyTrackerConfig(configurator: TrackerConfig<*>.() -> Unit) {
        trackerConfigModified = true
        dtoMap.values.forEach { it.tracker.config.configurator() }
        configurator.invoke(trackerConfig)
    }

    fun useDataModelBuilder(builderFn: () -> D): Unit = dtoFactory.setDataModelConstructor(builderFn)
}