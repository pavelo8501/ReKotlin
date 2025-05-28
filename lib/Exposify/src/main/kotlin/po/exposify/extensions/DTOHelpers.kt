package po.exposify.extensions

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.MultipleRepository
import po.exposify.dto.components.SingleRepository
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO

fun <DTO, DATA, ENTITY, F_DTO, FD, FE>  CommonDTO<DTO, DATA, ENTITY>.multipleRepository(
    childClass: DTOClass<F_DTO, FD, FE>
):MultipleRepository<DTO, DATA, ENTITY, F_DTO, FD, FE>?
    where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity,
          F_DTO:ModelDTO, FD: DataModel, FE : LongEntity{
   val oneToMany =  this.repositories.values.firstOrNull{ it.cardinality == Cardinality.ONE_TO_MANY }
   return oneToMany?.castOrOperationsEx("Unable to cast in multipleRepository helper")
}

fun <DTO, DATA, ENTITY, F_DTO, FD, FE>  CommonDTO<DTO, DATA, ENTITY>.singleRepository(
    childClass: DTOClass<F_DTO, FD, FE>
): SingleRepository<DTO, DATA, ENTITY, F_DTO, FD, FE>?
    where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity,
          F_DTO:ModelDTO, FD: DataModel, FE : LongEntity{
    val oneToOne =  this.repositories.values.firstOrNull{ it.cardinality == Cardinality.ONE_TO_ONE }
    return oneToOne?.castOrOperationsEx("Unable to cast in singleRepository helper")
}