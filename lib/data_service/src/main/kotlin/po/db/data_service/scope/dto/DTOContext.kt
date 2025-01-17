package po.db.data_service.scope.dto

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.components.eventhandler.models.Event
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CrudResult
import po.db.data_service.models.CommonDTO

/**
 * A context class used to encapsulate and simplify interaction with `CommonDTO`.
 *
 * `DTOContext` is designed to:
 * - Hide potentially harmful or irrelevant functions from the user.
 * - Provide a clean and intuitive DSL-friendly interface for working with a list of `CommonDTO` objects
 * and child entities
 *
 * This class is typically used as a receiver in higher-order functions to facilitate a DSL-like syntax.
 *
 * @param DATA The type of data model being processed.
 * @param ENTITY The type of entity represented by the `CommonDTO` instances.
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
    )  where DATA : DataModel, ENTITY : LongEntity {

    fun result(): List<CommonDTO<DATA, ENTITY>>{
        return  crudResult.rootDTOs
    }

    fun getStats(): Event?{
        crudResult.event?.print()
        return crudResult.event
    }

}