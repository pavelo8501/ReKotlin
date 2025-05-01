package po.exposify.dto.components.relation_binder.delegates

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.MultipleRepository
import po.exposify.dto.components.relation_binder.MultipleChildContainer
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


class OneToManyDelegate<DTO, DATA, ENTITY, C_DTO, CD, CF>(
    private val dto : CommonDTO<DTO, DATA, ENTITY>,
    private val childModel: DTOClass<C_DTO>,
    private val bindingContainer :  MultipleChildContainer<DTO, DATA, ENTITY, C_DTO, CD, CF>,
    private val multipleRepository: MultipleRepository<DTO, DATA, ENTITY, C_DTO, CD, CF>,
) : ReadOnlyProperty<DTO, List<CommonDTO<C_DTO, CD, CF>>>
        where DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
              C_DTO: ModelDTO,  CD : DataModel, CF : ExposifyEntity
{

    private val ownDataModels: KProperty1<DATA, List<*>> get() = bindingContainer.ownDataModelsProperty
    private val ownEntities: KProperty1<ENTITY, SizedIterable<*>> get() = bindingContainer.ownEntitiesProperty

    override fun getValue(thisRef: DTO, property: KProperty<*>): List<CommonDTO<C_DTO, CD, CF>>{
        return multipleRepository.getDtos()
    }
}