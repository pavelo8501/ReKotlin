package po.exposify.dto.models


//class CommonDTOType<DTO: ModelDTO, D: DataModel, E: LongEntity>(
//    kClass: KClass<CommonDTO<DTO, D, E>>,
//    kType: KType,
//) : TypeData<CommonDTO<DTO, D, E>>(kClass, kType) {
//
//
//    companion object{
//
//     inline  fun <reified DTO: ModelDTO, reified D: DataModel, reified E: LongEntity> create():CommonDTOType<DTO, D, E>{
//           val typeData = TypeData.create<CommonDTO<DTO, D, E>>()
//           println("CommonDTO Type ${typeData.typeName}")
//           return CommonDTOType(typeData.kClass, typeData.kType)
//        }
//    }
//}