package po.exposify.dto.interfaces

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.DataModelContainer

interface ModelDTO {
    val dataContainer  : DataModelContainer<*>
}