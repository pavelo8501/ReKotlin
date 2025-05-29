package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.components.tracker.TrackerConfig
import po.misc.reflection.properties.PropertyMap
import po.misc.registries.type.TypeRegistry

class DTOConfig<DTO, DATA, ENTITY>(
    val registry: TypeRegistry,
    val propertyMap : PropertyMap,
    val entityModel: ExposifyEntityClass<ENTITY>,
    val dtoClass : DTOBase<DTO, DATA , ENTITY>,
) where DTO: ModelDTO, DATA: DataModel,  ENTITY : LongEntity{

   internal var dtoFactory: DTOFactory<DTO, DATA, ENTITY> = DTOFactory(dtoClass, registry)
   internal var daoService :  DAOService<DTO, DATA, ENTITY> =  DAOService(dtoClass, registry)

    internal var trackerConfigModified : Boolean = false
    val trackerConfig : TrackerConfig = TrackerConfig()

    val childClasses : MutableList<DTOClass<*,*,*>> = mutableListOf()

   internal fun  addHierarchMemberIfAbsent(childDTO : DTOClass<*, *, *>) {
       if (!childDTO.initialized) {
           childDTO.initialization()
       }
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