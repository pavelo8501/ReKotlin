package po.exposify.scope.sequence.classes


import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.reflection.properties.findPropertiesOfType

@PublishedApi
internal fun<DTO: ModelDTO, D: DataModel, E: LongEntity>  forceHandlerProviderResolution(companion: DTOBase<DTO, D, E>) {
    when(companion){
        is RootDTO<DTO, D, E>->{
            val instanceDelegates  = companion::class.findPropertiesOfType<RootHandlerProvider<DTO, D, E>>()
            instanceDelegates.forEach {property->
                property.get(companion)
            }
        }
        is DTOClass<DTO , D, E>->{
            val instanceDelegates  =  companion::class.findPropertiesOfType<SwitchHandlerProvider<DTO, D, E, *, *, *>>()
            instanceDelegates.forEach {property->
                property.get(companion)
            }
        }
    }
}