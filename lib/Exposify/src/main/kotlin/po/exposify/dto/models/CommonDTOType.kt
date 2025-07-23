package po.exposify.dto.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.types.TypeData
import kotlin.reflect.KClass
import kotlin.reflect.KType


class CommonDTOType<DTO: ModelDTO, D: DataModel, E: LongEntity>(
    kClass: KClass<CommonDTO<DTO, D, E>>,
    kType: KType,
) : TypeData<CommonDTO<DTO, D, E>>(kClass, kType) {


    companion object{
       fun <DTO: ModelDTO, D: DataModel, E: LongEntity> create():CommonDTOType<DTO, D, E>{

           val typeData = TypeData.create<CommonDTO<DTO, D, E>>()
           println("CommonDTO Type ${typeData.typeName}")
           return CommonDTOType(typeData.kClass, typeData.kType)
        }
    }
}