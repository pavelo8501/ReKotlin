package po.exposify.dto.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import kotlin.reflect.KClass


interface DTORegistry<DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity> {
    val dataKClass:  KClass<DATA>
    val entityKClass: KClass<ENTITY>
    val derivedDTOClazz: KClass<DTO>

    val dataName get() =   dataKClass.simpleName.toString()
    val dtoName get() =   derivedDTOClazz.simpleName.toString()
    val entityName get() = entityKClass.simpleName.toString()


    val commonDTOQualifiedName get() = "CommonDTO[${dtoName}]"
    val dtoClassQualifiedName get() = "DTOClass[${dtoName}]"
    val rootDTOQualifiedName get() = "DTORoot[${dtoName}]"

}

data class DTORegistryItem<DTO, DATA, ENTITY>(
    override val dataKClass:  KClass<DATA>,
    override val entityKClass: KClass<ENTITY>,
    override val derivedDTOClazz: KClass<DTO>,
): DTORegistry<DTO, DATA, ENTITY> where DTO : ModelDTO, DATA: DataModel , ENTITY: LongEntity{



    val typeKeyDto: String get() = derivedDTOClazz.qualifiedName.toString()
    val typeKeyDataModel: String get() = dataKClass.qualifiedName.toString()
    val typeKeyEntity : String get() = entityKClass.qualifiedName.toString()
    val typeKeyCombined: String get() = "$typeKeyDto:$typeKeyDataModel:$typeKeyEntity"
}

