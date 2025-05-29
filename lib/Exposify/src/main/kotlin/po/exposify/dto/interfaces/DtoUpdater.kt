package po.exposify.dto.interfaces

import org.jetbrains.exposed.dao.LongEntity

/**
 *  Handles update logic of internal structures FROM DTO instance
 */
interface DtoUpdater<DTO : ModelDTO, D : DataModel, E : LongEntity> {

    /**
     * Pushes DTO values into existing entity & data model.
     */
    fun update(dto: DTO)

    /**
     * Synchronizes child collections and relations
     */
    fun syncRelations(dto: DTO)

    /**
     * Full sync: props + relations
     */
    fun syncAll(dto: DTO) {
        update(dto)
        syncRelations(dto)
    }
}