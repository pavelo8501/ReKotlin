package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.components.relation_binder.RelationshipBinder
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.dao.classes.ExposifyEntityClass

class DTOConfig<DTO, DATA, ENTITY>(
    val registryRecord : DTORegistryItem<DTO, DATA, ENTITY>,
    val entityModel: ExposifyEntityClass<ENTITY>,
    val dtoClass : DTOBase<DTO, DATA , ENTITY>
) where DTO: ModelDTO, DATA: DataModel,  ENTITY : LongEntity{

   internal var dtoFactory: DTOFactory<DTO, DATA, ENTITY> = DTOFactory(registryRecord.derivedDTOClazz, registryRecord.dataKClass, this)
   internal var daoService :  DAOService<DTO, DATA, ENTITY> =  DAOService(dtoClass, registryRecord)

    val relationBinder: RelationshipBinder<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>
            = RelationshipBinder(dtoClass)

    suspend fun hierarchyMembers(vararg childDTO : DTOClass<*, *, *>){
        childDTO.toList().forEach {
            relationBinder.addChildClass(it)
        }
    }

    fun useDataModelBuilder(builderFn: () -> DATA): Unit
            = dtoFactory.setDataModelConstructor(builderFn)

}