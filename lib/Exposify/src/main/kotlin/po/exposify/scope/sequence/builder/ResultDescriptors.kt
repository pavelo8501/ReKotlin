package po.exposify.scope.sequence.builder

import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO


sealed class SingleResultMarker

object SelectSingle:SingleResultMarker()
object InsertSingle:SingleResultMarker()
object UpdateSingle:SingleResultMarker()
object PickById:SingleResultMarker()

sealed class ListResultMarker

object Select:ListResultMarker()
