package po.exposify.dto.interfaces

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.DataModelContainer2

interface ModelDTO : DataModel   {
    val dataContainer  : DataModelContainer2<*>

}