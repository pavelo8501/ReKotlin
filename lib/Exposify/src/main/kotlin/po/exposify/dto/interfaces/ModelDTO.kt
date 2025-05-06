package po.exposify.dto.interfaces

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.property_binder.PropertyBinder
import po.exposify.dto.models.DTOTracker

interface ModelDTO : DataModel {

    val dtoName : String
    val dataModel: DataModel
    val dataContainer  : DataModelContainer<*, *>
    val daoService : DAOService<*, *, *>
    val propertyBinder : PropertyBinder<*, *>
    val dtoTracker: DTOTracker<*,*>
}


interface ModelDTO2<DATA: DataModel, ENTITY: LongEntity>{

    val personalName: String
    val dataModel: DATA
    val dataContainer  : DataModelContainer<ModelDTO, DATA>
    val daoService : DAOService<ModelDTO,  DATA, ENTITY>
    val propertyBinder : PropertyBinder<DATA, ENTITY>

}

