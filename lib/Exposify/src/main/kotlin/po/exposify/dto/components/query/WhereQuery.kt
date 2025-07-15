package po.exposify.dto.components.query

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import po.exposify.dto.DTOBase

//class DeferredWhere<T : LongIdTable>(private val block: () -> WhereQuery<T>) {
//    fun resolve(): WhereQuery<T> = block()
//}
//
//fun <T : LongIdTable> deferredWhere(block: () -> WhereQuery<T>): DeferredWhere<T> =
//    DeferredWhere(block)


class WhereQuery<E>(
   private val dtoClass: DTOBase<*, *, E>
): SimpleQuery() where E: LongEntity {

    //override  var expression: Set<Op<Boolean>> = emptySet()

    override var expression: MutableList<Op<Boolean>> = mutableListOf()

    private fun addCondition(condition: Op<Boolean>) {
        expression.add(condition)
      //  expression = expression + condition
    }


    val table: IdTable<Long> get(){
      return  dtoClass.config.entityModel.table
    }

    val tableAsLongId: LongIdTable get(){
        return  dtoClass.config.entityModel.table as LongIdTable
    }

    fun byId(id: Long): WhereQuery<E> {
        addCondition(table.id eq id)
        return this
    }
    fun <V> equals(column: Column<V> , value: V): WhereQuery<E> {
        addCondition(column eq value)
        return this
    }

    fun <V> equalsColumn(value: V, column: IdTable<Long>.() -> Column<V>): WhereQuery<E> {
        addCondition(table.column() eq value)
        return this
    }

    fun <V : Comparable<V>> greaterThan(column: IdTable<Long>.() -> Column<V>, value: V): WhereQuery<E> {
        addCondition(table.column().greater(value))
        return this
    }


    fun <V : Comparable<V>> greaterOrEquals(column: IdTable<Long>.() -> Column<V>, value: V): WhereQuery<E> {
        addCondition(table.column().greaterEq(value))
        return this
    }

    fun <V : Comparable<V>> lessThan(column: IdTable<Long>.() -> Column<V>, value: V): WhereQuery<E> {
        addCondition(table.column().less(value))
        return this
    }

    fun <V : Comparable<V>> lessOrEquals(value: V, column: IdTable<Long>.() -> Column<V>): WhereQuery<E> {
        addCondition(table.column().lessEq(value))
        return this
    }

    fun  likeString(value: String, column: IdTable<Long>.() -> Column<String>): WhereQuery<E> {
        addCondition(table.column().like(value))
        return this
    }
}


