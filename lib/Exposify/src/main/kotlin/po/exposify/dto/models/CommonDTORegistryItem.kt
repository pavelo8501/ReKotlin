package po.exposify.dto.models

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import kotlin.reflect.KClass

data class CommonDTORegistryItem<DTO, DATA, ENTITY>(
    val dtoClass : DTOClass<DTO>,
    val dataKClass:  KClass<DATA>,
    val entityKClass: KClass<ENTITY>,
    val commonDTOKClass: KClass<out CommonDTO<DTO, DATA, ENTITY>>,
    val owner: CommonDTO<DTO,DATA,ENTITY>
) where DTO : ModelDTO, DATA: DataModel , ENTITY: ExposifyEntityBase{

   // lateinit  var commonDTOKClass: KClass<out CommonDTO2<DTO, DATA, ENTITY>>

    val typeKeyDto: String get() = commonDTOKClass.qualifiedName.toString()
    val typeKeyDataModel: String get() = dataKClass.qualifiedName.toString()
    val typeKeyEntity : String get() = entityKClass.qualifiedName.toString()
    val typeKeyCombined: String get() = "$typeKeyDto:$typeKeyDataModel:$typeKeyEntity"
}