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
import po.exposify.dto.CommonDTO
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO


class WhereQuery<T> () : Query() where T : IdTable<Long> {

    override  var expression: Set<Op<Boolean>> = emptySet()

    private fun addCondition(condition: Op<Boolean>) {
        expression = expression + condition
    }

    fun <V> equalsTo(column: Column<V>, value: V): WhereQuery<T>  {
        addCondition(column eq value)
        return this
    }

    fun <V : Comparable<V>> greaterThan(column: Column<V>, value: V): WhereQuery<T> {
        addCondition(column.greater(value))
        return this
    }

    fun <V : Comparable<V>> greaterOrEquals(column: Column<V>, value: V): WhereQuery<T> {
        addCondition(column.greaterEq(value))
        return this
    }

    fun <V : Comparable<V>> lessThan(column: Column<V>, value: V): WhereQuery<T>  {
        addCondition( column.less(value))
        return this
    }

    fun <V : Comparable<V>> lessOrEquals(column: Column<V>, value: V): WhereQuery<T>  {
        addCondition( column.lessEq(value))
        return this
    }

    fun likeString(column: Column<String>, value: String?): WhereQuery<T> {
        if (value != null) {
            addCondition( column like value)
        }
        return this
    }
}

class SwitchQuery<DTO: ModelDTO, D: DataModel, E: LongEntity> (val dtoClass: RootDTO<DTO, D, E>, val id: Long) :  Query() {

    override  var expression: Set<Op<Boolean>> = emptySet()

   // var selectedDTO : CommonDTO<DTO, D, E>? = null
      //  private set


    internal suspend fun resolveQuery():CommonDTO<DTO, D, E>?{
        return dtoClass.serviceContext.pickById(id).getDTO()
    }

//    suspend fun idEqualsTo(id: Long):SwitchQuery<DTO, D, E>{
//        val dto = dtoClass.pickById(id).getDTO()
//        selectedDTO = dto
//        return this
//    }

}

sealed class Query() {

    abstract val expression: Set<Op<Boolean>>
    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
    }
    fun build(): Op<Boolean> {
        return combineConditions(expression)
    }
}
