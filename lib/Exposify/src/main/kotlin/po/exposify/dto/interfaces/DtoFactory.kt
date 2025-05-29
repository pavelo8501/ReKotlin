package po.exposify.dto.interfaces

import org.jetbrains.exposed.dao.LongEntity

/*
 * DTO-Centric CRUD Interface Definitions
 * Pavel's Exposify-style architecture
 */

interface DtoFactory<DTO : ModelDTO, D : DataModel, E : LongEntity> {

    /**
     * SELECT: Creates DTO from existing entity (used in reading from DB)
     */
    fun fromEntity(entity: E): DTO

    /**
     * INSERT: Creates DTO from external input data
     */
    fun fromData(dataModel: D): DTO

    /**
     * Creates Entity from DTO (typically for insert)
     */
    fun createEntity(dto: DTO): E

    /**
     * Creates DataModel from DTO (for serialization or inspection)
     */
    fun extractDataModel(dto: DTO): D
}
