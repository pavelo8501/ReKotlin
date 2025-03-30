package po.exposify.dto.models

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.classes.DTOClass2
import kotlin.reflect.KClass


data class DTORegistryItem<DATA, ENTITY>(
    val dtoKClass: KClass<out CommonDTO<DATA, ENTITY>>,
    val dataKClass:  KClass<DATA>,
    val entityKClass: KClass<ENTITY>,
    val dtoClass: DTOClass<*, *>
) where DATA: DataModel , ENTITY: LongEntity{



    val typeKeyDto: String get() = dtoClass.personalName
    val typeKeyDataModel: String get() = dataKClass.qualifiedName.toString()
    val typeKeyEntity : String get() = entityKClass.qualifiedName.toString()
    val typeKeyCombined: String get() = "$typeKeyDto:$typeKeyDataModel:$typeKeyEntity"
}