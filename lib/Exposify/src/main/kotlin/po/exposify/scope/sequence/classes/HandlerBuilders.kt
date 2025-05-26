package po.exposify.scope.sequence.classes

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import kotlin.properties.ReadOnlyProperty

fun <DTO: ModelDTO, D: DataModel, E: LongEntity>  RootDTO<DTO, D, E>.handler(): RootHandlerProvider<DTO, D, E> {
    return RootHandlerProvider(this)
}


fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity>  DTOClass<DTO, D, E>.handler(
    cardinality: Cardinality,
    rootSequenceHandler: RootHandlerProvider<F_DTO, FD, FE>,
): SwitchHandlerProvider<DTO, D, E, F_DTO, FD, FE>{
    return SwitchHandlerProvider(this, cardinality, rootSequenceHandler)
}
