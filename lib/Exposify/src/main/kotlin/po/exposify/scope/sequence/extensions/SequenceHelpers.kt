package po.exposify.scope.sequence.extensions

import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.scope.service.ServiceContext
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.sequence.enums.SequenceID


fun <DTO : ModelDTO, DATA: DataModel> ServiceContext<DTO, DATA, ExposifyEntity>.createHandler(
    sequenceID: SequenceID
): SequenceHandler<DTO, DATA>{
    val id = SequenceID.asValue(sequenceID)
    return  SequenceHandler(dtoClass.personalName, id)
}
