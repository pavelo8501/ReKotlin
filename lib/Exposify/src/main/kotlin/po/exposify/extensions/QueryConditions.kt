package po.exposify.extensions

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and


class WhereCondition<T> () : QueryConditions<T>() where T : IdTable<Long> {

    override  var expression: Set<Op<Boolean>> = emptySet()

    private fun addCondition(condition: Op<Boolean>) {
        expression = expression + condition
    }

    fun <V> equalsTo(column: Column<V>, value: V): WhereCondition<T>  {
        addCondition(column eq value)
        return this
    }

    fun <V : Comparable<V>> greaterThan(column: Column<V>, value: V): WhereCondition<T> {
        addCondition(column.greater(value))
        return this
    }

    fun <V : Comparable<V>> greaterOrEquals(column: Column<V>, value: V): WhereCondition<T> {
        addCondition(column.greaterEq(value))
        return this
    }

    fun <V : Comparable<V>> lessThan(column: Column<V>, value: V): WhereCondition<T>  {
        addCondition( column.less(value))
        return this
    }

    fun <V : Comparable<V>> lessOrEquals(column: Column<V>, value: V): WhereCondition<T>  {
        addCondition( column.lessEq(value))
        return this
    }

    fun likeString(column: Column<String>, value: String?): WhereCondition<T> {
        if (value != null) {
            addCondition( column like value)
        }
        return this
    }
}

sealed class QueryConditions<T>()  where T : IdTable<Long> {

    abstract val expression: Set<Op<Boolean>>
    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
    }
    fun build(): Op<Boolean> {
        return combineConditions(expression)
    }
}