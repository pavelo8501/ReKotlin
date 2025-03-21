package po.exposify.scope.condition

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.extensions.WhereCondition
import po.exposify.models.CrudResult
import po.exposify.scope.dto.DTOContext



class ConditionContext<T,  DATA, ENTITY>(
    private val  rootDTOClass: DTOClass<DATA, ENTITY>
) where T: IdTable<Long>,  DATA : DataModel, ENTITY : LongEntity {
    public var thisCondition:  WhereCondition<T> ? = null

    operator fun invoke(condition:  WhereCondition<T>): ConditionContext<T,  DATA, ENTITY> {
        thisCondition = condition
        return this
    }

    fun withConditions(condition:  WhereCondition<T>): ConditionContext<T, DATA, ENTITY> {
        thisCondition = condition
        return this
    }

    suspend fun select(){
      val result = if (thisCondition != null) {
           rootDTOClass.select(thisCondition!!)
        }else{
           rootDTOClass.select()
        }
         DTOContext<DATA,ENTITY>(rootDTOClass, result)
    }
}