package po.exposify.dto.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import kotlin.reflect.KClass


interface DTORegistry<DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity> {
    val dataKClass:  KClass<DATA>?
    val entityKClass: KClass<ENTITY>?

    val dataKClassName get() =   dataKClass?.simpleName?:"DATA"
    val dataKClassQualifiedName get() = dataKClass?.qualifiedName?:"DATA[Uninitialized]"
    val entityKClassQualifiedName get() = entityKClass?.qualifiedName?:"ENTITY[Uninitialized]"

    val dataModelName  get() =  dataKClassName
    val dtoName  get() =  "CommonDTO[${dataKClassName}]"
    val dtoClassName  get() = "DTOClass[${dataKClassName}]"
    val dtoRootName  get() = "DTORoot[${dataKClassName}]"
}

data class DTORegistryItem<DTO, DATA, ENTITY>(
    override val dataKClass:  KClass<DATA>,
    override val entityKClass: KClass<ENTITY>,
    val commonDTOKClass: KClass<out CommonDTO<DTO, DATA, ENTITY>>,
): DTORegistry<DTO, DATA, ENTITY> where DTO : ModelDTO, DATA: DataModel , ENTITY: LongEntity{



    val typeKeyDto: String get() = commonDTOKClass.qualifiedName.toString()
    val typeKeyDataModel: String get() = dataKClass.qualifiedName.toString()
    val typeKeyEntity : String get() = entityKClass.qualifiedName.toString()
    val typeKeyCombined: String get() = "$typeKeyDto:$typeKeyDataModel:$typeKeyEntity"
}


data class CommonDTORegistryItem<DTO, DATA, ENTITY>(
    override val dataKClass:  KClass<DATA>,
    override val entityKClass: KClass<ENTITY>,
    val commonDTOKClass: KClass<out CommonDTO<DTO, DATA, ENTITY>>,
    val owner: CommonDTO<DTO,DATA,ENTITY>
): DTORegistry<DTO, DATA, ENTITY> where DTO : ModelDTO, DATA: DataModel , ENTITY: LongEntity{

    val typeKeyDto: String get() = commonDTOKClass.qualifiedName.toString()
    val typeKeyDataModel: String get() = dataKClass.qualifiedName.toString()
    val typeKeyEntity : String get() = entityKClass.qualifiedName.toString()
    val typeKeyCombined: String get() = "$typeKeyDto:$typeKeyDataModel:$typeKeyEntity"
}
