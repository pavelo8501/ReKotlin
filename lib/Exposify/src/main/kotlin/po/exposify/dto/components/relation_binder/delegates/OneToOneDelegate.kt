package po.exposify.dto.components.relation_binder.delegates

import po.exposify.classes.DTOBase
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.SingleRepository
import po.exposify.dto.components.relation_binder.SingleChildContainer
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class OneToOneDelegate<DTO, DATA, ENTITY, C_DTO,  CD,  CF>(
    private val dto : CommonDTO<DTO, DATA, ENTITY>,
    private val childModel:DTOBase<C_DTO, *>,

    private val bindingContainer :  SingleChildContainer<DTO, DATA, ENTITY, C_DTO, CD, CF>,
    private val singleRepository: SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, CF>
) : ReadOnlyProperty<DTO, CommonDTO<C_DTO, CD, CF>?>
        where DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
              C_DTO: ModelDTO,  CD : DataModel, CF : ExposifyEntity
{
    private val ownDataModels: KProperty1<DATA, CD?> get() = bindingContainer.sourcePropertyWrapper.extract()
    private val ownEntities: KProperty1<ENTITY, CF> get() = bindingContainer.ownEntityProperty

    override fun getValue(thisRef: DTO, property: KProperty<*>): CommonDTO<C_DTO, CD, CF>? {
        return singleRepository.getDtos().singleOrNull()
    }
}