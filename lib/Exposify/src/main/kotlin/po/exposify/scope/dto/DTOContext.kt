package po.exposify.scope.dto

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.components.eventhandler.models.Event
import po.exposify.classes.interfaces.DataModel
import po.exposify.models.CrudResult

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
    private val  crudResult : CrudResult<DATA, ENTITY>,
    private val resultCallback: ((Any)-> Unit)? = null,
    )  where DATA : DataModel, ENTITY : LongEntity {


        init {
            resultCallback?.let{callbackOnResult(resultCallback)}
        }

        private fun extractData(crud: CrudResult<DATA, ENTITY>): List<DATA>{
            return  crud.rootDTOs.map { it.compileDataModel() }
        }

        fun getStats(): Event?{
            crudResult.event?.print()
            return crudResult.event
        }

        fun callbackOnResult(callback : (List<DATA>)->Unit ){
            callback.invoke(extractData(crudResult))
        }
}