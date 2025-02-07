package po.exposify.dto.functions

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.HostDTO

fun <DATA, ENTITY, CHILD_DATA, CHILD_ENTITY> List<HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>>
        .toCommonDtoList(): List<CommonDTO<DATA,ENTITY>>
where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity{
    return this.map { HostDTO.copyAsCommonDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(it) }
}