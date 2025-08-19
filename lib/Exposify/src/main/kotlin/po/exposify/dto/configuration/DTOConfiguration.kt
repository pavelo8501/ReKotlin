package po.exposify.dto.configuration

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.bindings.helpers.shallowDTO
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO

inline fun <reified DTO,  reified D, reified E> DTOBase<DTO, D, E>.configuration(
    noinline block:  DTOConfig<DTO, D, E>.() -> Unit
) where DTO: ModelDTO, D: DataModel, E: LongEntity {

    if (status == DTOClassStatus.Uninitialized) {
        block.invoke(dtoConfiguration)
        if(this is RootDTO<DTO, D, E>){
            updateStatus(DTOClassStatus.PreFlightCheck)

            val result =  setupValidation(shallowDTO())

            initializationComplete(result)
        }else{
            updateStatus(DTOClassStatus.PreFlightCheck)
        }
    }
}