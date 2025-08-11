package po.exposify.dto.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO


class ForeignDataModels<DTO: ModelDTO, D: DataModel, E: LongEntity>(
    val dataModels:List<D>,
    val foreignDTO: DTOClass<DTO, D, E>
){
    val commonDTOType: CommonDTOType<DTO, D, E> get() = foreignDTO.commonDTOType
}


class ForeignEntities<DTO: ModelDTO, D: DataModel, E: LongEntity>(
    val entities:List<E>,
    val foreignDTO: DTOClass<DTO, D, E>
){
    val commonDTOType: CommonDTOType<DTO, D, E> get() = foreignDTO.commonDTOType
}