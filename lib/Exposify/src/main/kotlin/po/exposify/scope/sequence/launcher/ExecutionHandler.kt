package po.exposify.scope.sequence.launcher

import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.builder.SingleDTOResult


sealed class ExecutionHandlerBase<DTO, D>(
    dtoClass: DTOBase<DTO, D, *>
) where DTO: ModelDTO, D: DataModel  {

    protected var thisName : String? = null
    private var isInitialized: Boolean = false
   // protected var valueBacking: ValueContainer<V>? = null
}


class ParametrizedSingleHandler<DTO, D, P> internal constructor(
    val dtoRoot: RootDTO<DTO, D, *>,
    val descriptor: SequenceDescriptorBase<DTO, D, P>
):ExecutionHandlerBase<DTO, D>(dtoRoot), SingleDTOResult<DTO, D>
        where DTO: ModelDTO, D: DataModel, P:D
{
    var result: ResultSingle<DTO, D, *>? = null

    companion object{
        fun <DTO: ModelDTO, D: DataModel, P:D> create(
            dtoRoot: RootDTO<DTO, D, *>,
            descriptor: SequenceDescriptorBase<DTO , D , P>
        ):ParametrizedSingleHandler<DTO, D, P>{
          return  ParametrizedSingleHandler(dtoRoot, descriptor)
        }
    }
}

