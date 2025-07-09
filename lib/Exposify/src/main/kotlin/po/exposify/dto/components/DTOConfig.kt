package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.components.tracker.models.TrackerConfig
import po.exposify.extensions.getOrInit
import po.misc.reflection.mappers.PropertyMapper
import po.misc.registries.type.TypeRegistry

class DTOConfig<DTO, DATA, ENTITY>(
    val dtoClass : DTOBase<DTO, DATA , ENTITY>,
) where DTO: ModelDTO, DATA: DataModel,  ENTITY : LongEntity{


    @PublishedApi
    internal var entityModelBacking: ExposifyEntityClass<ENTITY>? = null
    val entityModel: ExposifyEntityClass<ENTITY> get() = entityModelBacking.getOrInit("entityModel", dtoClass)

    @PublishedApi
    internal val propertyMap : PropertyMapper = PropertyMapper()

    @PublishedApi
    internal val registry: TypeRegistry = TypeRegistry()

   @PublishedApi
   internal val dtoFactory: DTOFactory<DTO, DATA, ENTITY> by lazy { DTOFactory(dtoClass) }
   internal val daoService :  DAOService<DTO, DATA, ENTITY>  by lazy { DAOService(dtoClass) }

    internal var trackerConfigModified : Boolean = false
    val trackerConfig : TrackerConfig = TrackerConfig()
    val childClasses : MutableList<DTOClass<*,*,*>> = mutableListOf()

   @PublishedApi
   internal fun addHierarchMember(childDTO : DTOClass<*, *, *>) {
       childClasses.add(childDTO)
   }

    fun  hierarchyMembers(vararg childDTO : DTOClass<*, *, *>){
        childDTO.toList().forEach {
            childClasses.add(it)
        }
    }

    fun applyTrackerConfig(configurator : TrackerConfig.()-> Unit){
        trackerConfigModified = true
        configurator.invoke(trackerConfig)
    }

    fun useDataModelBuilder(builderFn: () -> DATA): Unit
            = dtoFactory.setDataModelConstructor(builderFn)

}