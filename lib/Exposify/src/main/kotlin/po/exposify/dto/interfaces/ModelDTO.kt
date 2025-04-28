package po.exposify.dto.interfaces

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.property_binder.PropertyBinder

interface ModelDTO : DataModel {

    val personalName: String
    val dataModel: DataModel
    val dataContainer  : DataModelContainer<*, *>
    val daoService : DAOService<*, *>
    val propertyBinder : PropertyBinder<*, *>
}