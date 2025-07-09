package po.exposify.dto.interfaces

import po.exposify.dto.models.SourceObject


interface ResultingDTO<T: Any> {
    val simpleDTO:T
}