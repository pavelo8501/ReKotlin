package po.exposify.scope.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.extensions.WhereCondition
import po.exposify.models.CrudResult
import po.lognotify.eventhandler.models.Event

/**
 * A context class used to encapsulate and simplify interaction with `DTOFunctions`.
 *
 * `DTOContext` is designed to:
 * - Hide potentially harmful or irrelevant functions from the user.
 * - Provide a clean and intuitive DSL-friendly interface for working with a list of `DTOFunctions` objects
 * and child entities
 *
 * This class is typically used as a receiver in higher-order functions to facilitate a DSL-like syntax.
 *
 * @param DATA The type of data model being processed.
 * @param ENTITY The type of entity represented by the `DTOFunctions` instances.
 * @property crudResult an instance of CrudResult containing list of EntityDROs together with
 * the operation execution result represented by the Event class object
 *
 * Example usage:
 * ```
 * fun <DATA, ENTITY> DTOClass<DATA, ENTITY>.select(block: DTOContext<DATA, ENTITY>.() -> Unit) {
 *     dbQuery {
 *         select()
 *     }
 *     DTOContext(this).block()
 * }
 *
 * DTOClass<MyData, MyEntity>.select {
 *     // DSL operations here
 * }
 * ```
 */
class DTOContext<DATA, ENTITY>(
    private val rootDTOClass: DTOClass<DATA, ENTITY>,
    private var  crudResult : CrudResult<DATA, ENTITY>? = null,
    private val resultCallback: ((List<DATA> )-> Unit)? = null
    ) where DATA : DataModel, ENTITY : LongEntity {

    public var condition:  WhereCondition<*> ? = null

        init {
            resultCallback?.let{callbackOnResult(resultCallback)}
        }


        operator fun invoke(op:  WhereCondition<*>): DTOContext<DATA, ENTITY> {
            condition = op
            return this
        }

        fun <T: IdTable<Long>> withConditions(conditions :  WhereCondition<T>): DTOContext<DATA, ENTITY> {
            condition = conditions
            return this
        }

        suspend fun select() {
            crudResult =  if (condition != null) {
                rootDTOClass.select(condition!!)
            }else{
                rootDTOClass.select()
            }
        }

        private fun asDataModels(crud: CrudResult<DATA, ENTITY>): List<DATA>{
            return  crud.rootDTOs.map { it.compileDataModel() }
        }

        fun getData(): List<DATA>{
            return  asDataModels(crudResult!!)
        }

        fun getStats(): Event?{
            crudResult!!.event?.print()
            return crudResult!!.event
        }

        fun callbackOnResult(callback : (List<DATA>)->Unit ){
            callback.invoke(asDataModels(crudResult!!))
        }
}