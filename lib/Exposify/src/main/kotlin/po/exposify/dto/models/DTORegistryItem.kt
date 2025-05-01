package po.exposify.dto.models

import po.exposify.classes.DTOBase
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import kotlin.reflect.KClass






interface DTORegistry<DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity> {
    val dataKClass:  KClass<DATA>?
    val entityKClass: KClass<ENTITY>?


    val dataKClassName get() =   dataKClass?.simpleName?:"DATA"
    val dataKClassQualifiedName get() = dataKClass?.qualifiedName?:"DATA[Uninitialized]"
    val entityKClassQualifiedName get() = entityKClass?.qualifiedName?:"ENTITY[Uninitialized]"

    val dataModelName  get() =  dataKClassName
    val dtoName  get() =  "DTO[${dataKClassName}]"
    val dtoClassName  get() =  "DTOClass[${dataKClassName}]"
}


data class DTORegistryItem<DTO, DATA, ENTITY>(
    override val dataKClass:  KClass<DATA>,
    override val entityKClass: KClass<ENTITY>,
    val commonDTOKClass: KClass<out CommonDTO<DTO, DATA, ENTITY>>,
): DTORegistry<DTO, DATA, ENTITY> where DTO : ModelDTO, DATA: DataModel , ENTITY: ExposifyEntity{

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
): DTORegistry<DTO, DATA, ENTITY> where DTO : ModelDTO, DATA: DataModel , ENTITY: ExposifyEntity{

    val typeKeyDto: String get() = commonDTOKClass.qualifiedName.toString()
    val typeKeyDataModel: String get() = dataKClass.qualifiedName.toString()
    val typeKeyEntity : String get() = entityKClass.qualifiedName.toString()
    val typeKeyCombined: String get() = "$typeKeyDto:$typeKeyDataModel:$typeKeyEntity"
}
