package po.exposify.dto.components.property_binder.delegates

import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.InitException
import po.misc.types.castOrThrow
import kotlin.reflect.KMutableProperty1

inline fun <reified DATA, reified ENTITY, reified FOREIGN_ENTITY, DTO> ModelDTO.idReferenced(
    dataProperty : KMutableProperty1<DATA, Long>,
    entityProperty: KMutableProperty1<ENTITY, FOREIGN_ENTITY>,
    foreignDtoCals: DTOClass<DTO>
): ModelEntityIDDelegate<DATA, ENTITY, FOREIGN_ENTITY, DTO>
    where DATA:DataModel, ENTITY : ExposifyEntityBase, FOREIGN_ENTITY: ExposifyEntityBase, DTO: ModelDTO
{
    val castedThis = this.castOrThrow<CommonDTO<ModelDTO, DATA, ENTITY>, InitException>()
    return ModelEntityIDDelegate(castedThis, dataProperty, entityProperty, foreignDtoCals)
}

//inline fun <reified DATA, reified ENTITY, reified FOREIGN_ENTITY, DTO> ModelDTO.parentReference(
//    dataProperty : KMutableProperty1<DATA, Long>,
//    entityProperty: KMutableProperty1<ENTITY, FOREIGN_ENTITY>,
//    parentDtoClass :  DTOClass<DTO>
//): ModelEntityDelegate<DATA, ENTITY, FOREIGN_ENTITY,  DTO>
//    where DATA:DataModel, ENTITY: ExposifyEntityBase, FOREIGN_ENTITY:ExposifyEntityBase,  DTO: ModelDTO
//{
//    val castedThis = this.castOrThrow<CommonDTO<ModelDTO, DATA, ExposifyEntityBase>, InitException>()
//    return  ModelEntityDelegate(castedThis, dataProperty, entityProperty, parentDtoClass)
//}