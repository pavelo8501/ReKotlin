package po.exposify.scope.sequence.builder

import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO


sealed class SingleResultMarker

object SelectSingle:SingleResultMarker()
object InsertSingle:SingleResultMarker()
object PickById:SingleResultMarker()
object UpdateSingle:SingleResultMarker()

sealed class ListResultMarker

object Select:ListResultMarker()




//interface ListDTOResult<DTO: ModelDTO, D: DataModel>{
//    val result: ResultList<DTO, D, *>?
//}

