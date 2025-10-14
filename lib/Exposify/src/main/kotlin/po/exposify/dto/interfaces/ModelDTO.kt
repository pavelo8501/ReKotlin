package po.exposify.dto.interfaces

import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.models.CommonDTOType
import po.exposify.dto.models.DTOId
import po.misc.context.CTX

interface ModelDTO: DataModel, CTX {
    val cardinality: Cardinality
    val bindingHub: BindingHub<*, *, *>
    val daoService : DAOService<*, *, *>
    val dtoFactory: DTOFactory<*, *, *>
    val tracker: DTOTracker<*, *, *>
    val dtoId : DTOId<*>
    //val typeData: TypeData<*>
    val commonType: CommonDTOType<*, *, *>

    fun flush()
    fun flush(dataModel: DataModel):Boolean
}


