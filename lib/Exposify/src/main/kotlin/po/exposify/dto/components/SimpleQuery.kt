package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
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
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO


sealed class SimpleQuery() {
    abstract val expression: Set<Op<Boolean>>
    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
    }
    fun build(): Op<Boolean> {
        return combineConditions(expression)
    }
}


class WhereQuery<T> (
    private val table: T
) : SimpleQuery() where T : IdTable<Long> {
    override  var expression: Set<Op<Boolean>> = emptySet()
    private fun addCondition(condition: Op<Boolean>) {
        expression = expression + condition
    }

    fun byId(id: Long): WhereQuery<T> {
        addCondition(table.id eq id)
        return this
    }

    fun <V> equals(column: Column<V> , value: V): WhereQuery<T> {
        addCondition(column eq value)
        return this
    }

    fun <V> equalsTo(column: T.() -> Column<V>, value: V): WhereQuery<T> {
        addCondition(table.column() eq value)
        return this
    }

    fun <V : Comparable<V>> greaterThan(column: T.() -> Column<V>, value: V): WhereQuery<T> {
        addCondition(table.column().greater(value))
        return this
    }


    fun <V : Comparable<V>> greaterOrEquals(column: T.() -> Column<V>, value: V): WhereQuery<T> {
        addCondition(table.column().greaterEq(value))
        return this
    }


    fun <V : Comparable<V>> lessThan(column: T.() -> Column<V>, value: V): WhereQuery<T> {
        addCondition(table.column().less(value))
        return this
    }


    fun <V : Comparable<V>> lessOrEquals(column: T.() -> Column<V>, value: V): WhereQuery<T> {
        addCondition(table.column().lessEq(value))
        return this
    }

    fun  likeString(column: T.() -> Column<String>, value: String): WhereQuery<T> {
        addCondition(table.column().like(value))
        return this
    }
}

class SwitchQuery<DTO: ModelDTO, D : DataModel, E: LongEntity>(
    private val lookUpId: Long,
    private val dtoClass: RootDTO<DTO,D,E>,
):SimpleQuery() {

    override var expression: Set<Op<Boolean>> = emptySet()

    init {
        addCondition(dtoClass.config.entityModel.sourceTable.id eq lookUpId)
    }

    private fun addCondition(condition: Op<Boolean>) {
        expression = expression + condition
    }

    suspend fun resolve(): ResultSingle<DTO, D, E> {
        val existent = dtoClass.lookupDTO(lookUpId)
        return if (existent != null) {
            dtoClass.createSingleResult(existent)
        } else {
            dtoClass.createExecutionProvider().pick(this)
        }
    }

}

