package po.exposify.dto.interfaces

import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.models.Component
import po.misc.types.TypeRecord

interface ModelDTO : DataModel {
    val cardinality: Cardinality
    val dataModel: DataModel
    //val dataContainer  : DataModelContainer<*, *>
    val daoService : DAOService<*, *, *>
    val dtoFactory: DTOFactory<*, *, *>
    val tracker: DTOTracker<*,*>
    val dtoType: TypeRecord<*>
}


