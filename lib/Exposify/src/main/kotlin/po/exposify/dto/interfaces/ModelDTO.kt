package po.exposify.dto.interfaces

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.property_binder.PropertyBinder
import po.exposify.entity.classes.ExposifyEntityBase

interface ModelDTO : DataModel {

    val personalName: String
    val dataModel: DataModel
    val dataContainer  : DataModelContainer<*, *>
    val daoService : DAOService<*, *>
    val propertyBinder : PropertyBinder<*, *>
}


interface ModelDTO2<DATA: DataModel, ENTITY: ExposifyEntityBase>{

    val personalName: String
    val dataModel: DATA
    val dataContainer  : DataModelContainer<ModelDTO, DATA>
    val daoService : DAOService<ModelDTO, ENTITY>
    val propertyBinder : PropertyBinder<DATA, ENTITY>

}

