package po.exposify.dto.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO2
import po.exposify.dto.interfaces.ModelDTO
import kotlin.reflect.KClass

internal data class DtoClassRegistryItem2<DTO, DATA, ENTITY>(
    val dtoKClass:  KClass<out CommonDTO2<DTO, DATA, ENTITY>>,
    val dataKClass:  KClass<DATA>,
    val entityKClass: KClass<ENTITY>
) where DTO : ModelDTO, DATA: DataModel , ENTITY: LongEntity{

    val typeKeyDto: String get() = dtoKClass.qualifiedName.toString()
    val typeKeyDataModel: String get() = dataKClass.qualifiedName.toString()
    val typeKeyEntity : String get() = entityKClass.qualifiedName.toString()
    val typeKeyCombined: String get() = "$typeKeyDto:$typeKeyDataModel:$typeKeyEntity"
}