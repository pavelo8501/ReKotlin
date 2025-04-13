package po.exposify.dto.models

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import kotlin.reflect.KClass



data class DTORegistryItem<DTO ,DATA, ENTITY>(
    val commonDTOKClass: KClass<out CommonDTO<DTO, DATA, ENTITY>>,
    val dataKClass: KClass<DATA>,
    val entityKClass: KClass<ENTITY>,
    val dtoClass: DTOClass<DTO>
) where DTO: ModelDTO, DATA: DataModel , ENTITY: ExposifyEntityBase{

    fun getDataEntityTypePair(): Pair<KClass<DATA>, KClass<ENTITY>>{
        return Pair(dataKClass, entityKClass)
    }

    val typeKeyDto: String get() = dtoClass.personalName
    val typeKeyDataModel: String get() = dataKClass.qualifiedName.toString()
    val typeKeyEntity : String get() = entityKClass.qualifiedName.toString()
    val typeKeyDataEntity: String get() = "${dataKClass.qualifiedName.toString()}:${entityKClass.qualifiedName.toString()}"
    val typeKeyCombined: String get() = "$typeKeyDto:$typeKeyDataModel:$typeKeyEntity"
}