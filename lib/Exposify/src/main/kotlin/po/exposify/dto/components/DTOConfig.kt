package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.DatabaseManager
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.tracker.models.TrackerConfig
import po.exposify.dto.models.CommonDTOType
import po.exposify.exceptions.badDTOSetup
import po.exposify.extensions.getOrInit
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.lognotify.TasksManaged
import po.lognotify.enums.SeverityLevel
import po.misc.containers.LazyBackingContainer
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.context.asSubIdentity
import po.misc.functions.common.ExceptionFallback
import po.misc.registries.type.TypeRegistry
import po.misc.serialization.SerializerInfo
import po.misc.types.toSimpleNormalizedKey
import kotlin.reflect.KType


interface DTOConfiguration<DTO: ModelDTO, D: DataModel,  E : LongEntity> {

    fun <F: ModelDTO, FD: DataModel, FE: LongEntity> addHierarchMember(
        parent: DTOBase<DTO, D, E>,
        childMember: DTOClass<F, FD, FE>
    ):DTOClass<F, FD, FE>

    fun serializerLookup(type: KType): SerializerInfo<*>?
}


class DTOConfig<DTO, D, E>(
    val commonDTOType:CommonDTOType<DTO, D, E>
):DTOConfiguration<DTO, D, E>, TasksManaged where DTO: ModelDTO, D: DataModel, E : LongEntity {

    override val identity: CTXIdentity<DTOConfig<DTO, D, E>> = asIdentity()

    @PublishedApi
    internal val dtoFactory: DTOFactory<DTO, D, E> = DTOFactory(this)
    internal val daoService: DAOService<DTO, D, E> = DAOService(commonDTOType)

    internal var trackerConfigModified: Boolean = false
    val trackerConfig: TrackerConfig = TrackerConfig()
    val childClasses: MutableMap<CommonDTOType<*, *, *>, DTOClass<*, *, *>> = mutableMapOf()
    val connectionClass: ConnectionClass? get() = DatabaseManager.connections.firstOrNull()

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

    fun applyTrackerConfig(configurator: TrackerConfig.() -> Unit) {
        trackerConfigModified = true
        configurator.invoke(trackerConfig)
    }

    fun useDataModelBuilder(builderFn: () -> D): Unit = dtoFactory.setDataModelConstructor(builderFn)
}