package po.exposify.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Column
import po.exposify.entity.classes.ExposifyEntityClass

fun <E: LongEntity> ExposifyEntityClass<E>.columns(): List<Column<*>>{
  return  this.sourceTable.columns
}