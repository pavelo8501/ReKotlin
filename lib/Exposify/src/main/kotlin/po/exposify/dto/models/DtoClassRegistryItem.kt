package po.exposify.dto.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import kotlin.reflect.KClass


internal data class DtoClassRegistryItem<DATA, ENTITY>(
    val dtoKClass:  KClass<out CommonDTO<DATA, ENTITY>>,
    val dataKClass:  KClass<DATA>,
    val entityKClass: KClass<ENTITY>
) where DATA: DataModel , ENTITY: LongEntity{

    val typeKeyDto: String get() = dtoKClass.qualifiedName.toString()
    val typeKeyDataModel: String get() = dataKClass.qualifiedName.toString()
    val typeKeyEntity : String get() = entityKClass.qualifiedName.toString()
    val typeKeyCombined: String get() = "$typeKeyDto:$typeKeyDataModel:$typeKeyEntity"
}