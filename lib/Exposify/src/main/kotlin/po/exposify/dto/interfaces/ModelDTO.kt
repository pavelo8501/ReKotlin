package po.exposify.dto.interfaces

import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.property_binder.PropertyBinder
import po.exposify.entity.classes.ExposifyEntity

interface ModelDTO : DataModel {

    val personalName: String
    val dataModel: DataModel
    val dataContainer  : DataModelContainer<*, *>
    val daoService : DAOService<*, *, *>
    val propertyBinder : PropertyBinder<*, *>
}


interface ModelDTO2<DATA: DataModel, ENTITY: ExposifyEntity>{

    val personalName: String
    val dataModel: DATA
    val dataContainer  : DataModelContainer<ModelDTO, DATA>
    val daoService : DAOService<ModelDTO,  DATA, ENTITY>
    val propertyBinder : PropertyBinder<DATA, ENTITY>

}

