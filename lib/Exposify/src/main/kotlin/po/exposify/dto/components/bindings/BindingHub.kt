package po.exposify.dto.components.bindings

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.bindings.property_binder.delegates.AttachedForeignDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ParentDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.RelationDelegate
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.ExposifyModule
import po.exposify.dto.models.ModuleType
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableModule
import po.misc.interfaces.ValueBased
import po.misc.reflection.mappers.models.PropertyContainer
import po.misc.reflection.properties.toRecord
import po.misc.registries.callback.TypedCallbackRegistry


class BindingHub<DTO, D, E, F_DTO, FD, FE>(
    val hostingDTO: CommonDTO<DTO, D, E>,
    val moduleType : ExposifyModule = ExposifyModule(ModuleType.BindingHub, hostingDTO)
): IdentifiableModule by moduleType
        where  DTO : ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{
    data class NotificationData<T: Any>(
        val delegateName: String,
        val propertyRecord: PropertyContainer<T>
    )

    enum class Event(override val value: Int): ValueBased{
        DelegateInitialized(1)
    }

    val qualifiedName: String = "BindingHub[${hostingDTO.componentName}]"
    val dtoClass : DTOBase<DTO, D, E>  get() = hostingDTO.dtoClass
    val daoService: DAOService<DTO, D, E> get() = hostingDTO.daoService

    private val notifier: TypedCallbackRegistry<NotificationData<*>, Unit> = TypedCallbackRegistry()
    private val responsiveDelegates : MutableMap<String, ResponsiveDelegate<DTO, D, E, *>>  = mutableMapOf()
    private val attachedForeignDelegates : MutableMap<String, AttachedForeignDelegate<DTO, D, E,  ModelDTO, *, *>>  = mutableMapOf()
    private val parentDelegates : MutableMap<String, ParentDelegate<DTO, D, E,  ModelDTO, *, *>>  = mutableMapOf()

    private val relationDelegates : MutableMap<String, RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>> = mutableMapOf()

    fun subscribe(identity: Identifiable, event: Event,  callback: (NotificationData<*>)-> Unit){
        notifier.subscribe(identity, event, callback)
    }

    fun setRelationBinding(
        binding: RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>
    ): RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>
    {
        relationDelegates[binding.completeName] = binding
        notifier.triggerForAll(Event.DelegateInitialized, NotificationData(binding.completeName, binding.property.toRecord()))
        return binding
    }


    fun setParentDelegate(
        binding: ParentDelegate<DTO, D, E,  ModelDTO,  *, *>
    ): ParentDelegate<DTO, D, E,  ModelDTO,  *,  *> {
        parentDelegates[binding.completeName] = binding
        notifier.triggerForAll(Event.DelegateInitialized,  NotificationData(binding.completeName, binding.property.toRecord()))
        return binding
    }

    fun setAttachedForeignDelegate(
        binding: AttachedForeignDelegate<DTO, D, E,  ModelDTO,  *, *>
    ): AttachedForeignDelegate<DTO, D, E,  ModelDTO,  *,  *> {
        attachedForeignDelegates[binding.completeName] = binding
        notifier.triggerForAll(Event.DelegateInitialized, NotificationData(binding.completeName, binding.property.toRecord()))
        return binding
    }

    fun setBinding(
        binding : ResponsiveDelegate<DTO, D, E, *>
    ): ResponsiveDelegate<DTO, D, E, *> {
        responsiveDelegates[binding.completeName] =  binding
        return  binding
    }

    fun getResponsiveDelegates(): List<ResponsiveDelegate<DTO, D, E, *>>{
        return this.responsiveDelegates.values.toList()
    }

    fun getRelationDelegates(cardinality: Cardinality = Cardinality.ONE_TO_MANY): List<RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>>{
        return this.relationDelegates.values.filter { it.cardinality == cardinality }.toList()
    }

    fun getAttachedForeignDelegates():List<AttachedForeignDelegate<DTO, D, E, *, *, *>>{
        return attachedForeignDelegates.values.toList()
    }

    fun getParentDelegates():List<ParentDelegate<DTO, D, E, *, *, *>>{
        return parentDelegates.values.toList()
    }

    /***
     * updateEntity update entity properties
     * relations are not yet assigned
     */
    internal fun updateEntity(entity: E) {
        responsiveDelegates.values.forEach { responsiveDelegate ->
            responsiveDelegate.updateDTOProperty(hostingDTO.dataModel, entity)
        }
    }

    /***
     * createChildByData child DTO creation with parent finalized
     */
    internal fun createChildByData(){
        getRelationDelegates(hostingDTO.cardinality).forEach {relationDelegate->
            relationDelegate.createByData()
        }
    }




//    internal fun <F_DTO2: ModelDTO, FD2: DataModel, FE2: LongEntity> createByData(
//        childDTO: CommonDTO<F_DTO2, FD2, FE2>,
//        bindFn: (FE2) -> Unit
//    ){
//        val insertedEntity = childDTO.daoService.saveWithParent { entity->
//            childDTO.bindingHub.responsiveDelegates.values.forEach {responsiveDelegate->
//                responsiveDelegate.updateDTOProperty(childDTO.dataModel, entity)
//            }
//            childDTO.bindingHub.getRelationDelegates().forEach { relationDelegate ->
//                relationDelegate.createByData()
//            }
//            bindFn.invoke(entity)
//        }
//        childDTO.provideInsertedEntity(insertedEntity)
//      //  childDTO.provideInsertedEntity(insertedEntity)
//    }

    fun createByEntity(){
        responsiveDelegates.values.forEach {responsiveDelegate->
            responsiveDelegate.updateDTOProperty(hostingDTO.getEntity(hostingDTO))
        }
        getRelationDelegates(hostingDTO.cardinality).forEach {relationDelegate->
            relationDelegate.createByEntity()
        }
    }

    fun updateFromData(data:D){
        responsiveDelegates.values.forEach {
            it.updateDTOProperty(data, hostingDTO.getEntity(hostingDTO))
        }
        getRelationDelegates(hostingDTO.cardinality).forEach {relationDelegate->
            relationDelegate.updateFromData(data)
        }
    }
}