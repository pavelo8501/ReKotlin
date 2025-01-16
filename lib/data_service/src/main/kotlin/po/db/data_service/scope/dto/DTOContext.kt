package po.db.data_service.scope.dto

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.EntityDTO

/**
 * A context class used to encapsulate and simplify interaction with `EntityDTO`.
 *
 * `DTOContext` is designed to:
 * - Hide potentially harmful or irrelevant functions from the user.
 * - Provide a clean and intuitive DSL-friendly interface for working with a list of `EntityDTO` objects
 * and child entities
 *
 * This class is typically used as a receiver in higher-order functions to facilitate a DSL-like syntax.
 *
 * @param DATA The type of data model being processed.
 * @param ENTITY The type of entity represented by the `EntityDTO` instances.
 * @property rootDTOs The list of `EntityDTO` objects that this context operates on.
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
    private val rootDTOs: List<EntityDTO<DATA, ENTITY>>,

    )  where DATA : DataModel, ENTITY : LongEntity {

    fun result(): List<EntityDTO<DATA, ENTITY>>{
        return  rootDTOs
    }

}