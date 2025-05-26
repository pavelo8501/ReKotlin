package po.exposify.dto.interfaces

import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.tracker.DTOTracker

interface ModelDTO : DataModel {
    val dtoName : String
    val dataModel: DataModel
    val dataContainer  : DataModelContainer<*, *>
    val daoService : DAOService<*, *, *>
    val dtoFactory: DTOFactory<*, *, *>
    val tracker: DTOTracker<*,*>
}


