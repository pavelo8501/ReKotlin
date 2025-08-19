package po.exposify.scope.service.models

import org.jetbrains.exposed.dao.id.LongIdTable

sealed interface TableCreateMode {

   object Create : TableCreateMode

    object ForceRecreate : TableCreateMode {

        internal var tables: List<LongIdTable> = listOf()
        fun withTables(vararg table: LongIdTable):ForceRecreate  {
            tables = table.toList()
            return this
        }
    }
}