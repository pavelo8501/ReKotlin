package po.exposify.dto.interfaces

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.id.IdTable

interface ClassDTO{

    var initialized : Boolean
    fun nowTime(): LocalDateTime {
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

    fun getAssociatedTables(cumulativeList: MutableList<IdTable<Long>>)
}

interface DataModel {
    var id : Long
}
