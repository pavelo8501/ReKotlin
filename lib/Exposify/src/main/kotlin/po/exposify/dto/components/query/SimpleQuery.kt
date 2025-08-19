package po.exposify.dto.components.query

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.and


fun Op<Boolean>.toSqlString(): String {
    val builder = QueryBuilder(prepared = false)
    this.toQueryBuilder(builder)
    return builder.toString()
}

abstract class SimpleQuery() {
   // abstract val expression: Set<Op<Boolean>>
    abstract val expression: MutableList<Op<Boolean>>

    private fun combineConditions(conditions: MutableList<Op<Boolean>>): Op<Boolean> {
        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
    }
    fun build(): Op<Boolean> {
        return combineConditions(expression)
    }
    override fun toString(): String {
       return build().toSqlString()
    }
}


//class SwitchQuery<DTO: ModelDTO, D : DataModel, E: LongEntity>(
//    private val lookUpId: Long,
//    private val dtoClass: RootDTO<DTO,D,E>
//):SimpleQuery() {
//
//    override var expression: Set<Op<Boolean>> = emptySet()
//
//    fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
//        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
//    }
//
//    init {
//        addCondition(dtoClass.config.entityModel.sourceTable.id eq lookUpId)
//    }
//
//    private fun addCondition(condition: Op<Boolean>) {
//        expression = expression + condition
//    }
//
//    fun resolve(executionContext: ExecutionContext<DTO, D, E>): ResultSingle<DTO, D, E> {
//       return dtoClass.lookupDTO(lookUpId)?.toResult(CrudOperation.Pick) ?:run {
//           //executionContext.pickById(lookUpId, executionContext)
//           TODO("Not")
//        }
//    }
//}

