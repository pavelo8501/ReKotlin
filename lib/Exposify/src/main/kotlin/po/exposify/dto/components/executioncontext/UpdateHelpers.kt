package po.exposify.dto.components.executioncontext

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.annotations.DTOExecutionContextHelpers
import po.exposify.dto.components.bindings.relation_binder.delegates.withEachDelegate
import po.exposify.dto.components.bindings.withDTOHub
import po.exposify.dto.components.bindings.withHostDTOHub
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.context.CTX
import po.misc.data.helpers.output
import po.misc.data.styles.Colour

internal fun <D: DataModel> splitData (
    dataList: List<D>
):SplitLists<D>{
    return SplitLists(dataList)
}


@DTOExecutionContextHelpers
fun <DTO, D,  E, F, FD, FE>  DTOExecutionContext<DTO, D,  E, F, FD, FE>.insertChildBinding(
    initiator: CTX
): List<CommonDTO<F, FD, FE>>  where DTO: ModelDTO, D: DataModel, E: LongEntity, F : ModelDTO, FD: DataModel, FE: LongEntity
{
    val resultingList = mutableListOf<CommonDTO<F, FD, FE>>()
    withHostDTOHub {
        getRelationDelegates(dtoClass).withEachDelegate {
            val dtoList = createDTOS { existent ->
                "existent triggered $existent".output(Colour.Cyan)
            }
            dtoList.forEach { commonDTO ->
                withDTOHub(commonDTO) {
                    val persisted = hostingDAO.save { entity ->
                        updateEntity(entity)
                    }
                    commonDTO.entityContainer.provideValue(persisted)
                    resultingList.add(commonDTO)
                }
            }
        }
    }
    return resultingList
}

@DTOExecutionContextHelpers
fun <DTO, D,  E, F, FD, FE>  DTOExecutionContext<DTO, D,  E, F, FD, FE>.reloadByEntity(
    initiator: CTX,
    entity:E,
): List<CommonDTO<F, FD, FE>>  where DTO: ModelDTO, D: DataModel, E: LongEntity, F : ModelDTO, FD: DataModel, FE: LongEntity
{
    val resultingList = mutableListOf<CommonDTO<F, FD, FE>>()
    withHostDTOHub {
        getRelationDelegates(dtoClass).withEachDelegate {
            val dtoList =createDTOS(entity){existent ->
               "reloadByEntity existent triggered $existent".output(Colour.Cyan)
            }
            resultingList.addAll(dtoList)
        }
    }
    return resultingList
}