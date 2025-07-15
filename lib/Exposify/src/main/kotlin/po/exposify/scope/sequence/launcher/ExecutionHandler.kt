package po.exposify.scope.sequence.launcher

import po.exposify.dto.DTOBase
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrOperations
import po.misc.functions.containers.DeferredContainer


sealed class ExecutionHandlerBase<DTO, D>(
    dtoClass: DTOBase<DTO, D, *>
) where DTO: ModelDTO, D: DataModel  {

    protected var thisName : String? = null
    private var isInitialized: Boolean = false
}


class SingleResultHandler<DTO, D, P> internal constructor(
    val descriptor: RootDescriptorBase<DTO, D, P>
):ExecutionHandlerBase<DTO, D>(descriptor.dtoClass)
        where DTO: ModelDTO, D: DataModel, P:D
{
    var result: ResultSingle<DTO, D, *>? = null

    companion object{
        fun <DTO: ModelDTO, D: DataModel, P:D> create(
            descriptor: RootDescriptorBase<DTO , D , P>
        ):SingleResultHandler<DTO, D, P>{
          return  SingleResultHandler(descriptor)
        }
    }
}


class ListResultHandler<DTO, D> internal constructor(
   private val descriptor: ListDescriptor<DTO, D>
):ExecutionHandlerBase<DTO, D>(descriptor.dtoClass)
        where DTO: ModelDTO, D: DataModel
{
    var result: ResultList<DTO, D, *>? = null

    private val deferredQueryError = "DeferredQuery required but not provided"
    private var deferredQueryBacking: DeferredContainer<WhereQuery<*>>? = null
    val whereQuery: DeferredContainer<WhereQuery<*>> get() =  deferredQueryBacking.getOrOperations(deferredQueryError)

    companion object{
        fun <DTO: ModelDTO, D: DataModel> create(
            descriptor: ListDescriptor<DTO, D>
        ):ListResultHandler<DTO, D>{
            return  ListResultHandler(descriptor)
        }
    }
}

