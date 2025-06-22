package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.components.tracker.TrackerConfig
import po.misc.reflection.mappers.PropertyMapper
import po.misc.registries.type.TypeRegistry

class DTOConfig<DTO, DATA, ENTITY>(
    val registry: TypeRegistry,
    val propertyMap : PropertyMapper,
    val entityModel: ExposifyEntityClass<ENTITY>,
    val dtoClass : DTOBase<DTO, DATA , ENTITY>,
) where DTO: ModelDTO, DATA: DataModel,  ENTITY : LongEntity{

   @PublishedApi
   internal var dtoFactory: DTOFactory<DTO, DATA, ENTITY> = DTOFactory(dtoClass, registry)
   internal var daoService :  DAOService<DTO, DATA, ENTITY> =  DAOService(dtoClass, registry)

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