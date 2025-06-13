package po.exposify.dto.components.bindings

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.bindings.interfaces.DelegateInterface
import po.exposify.dto.components.bindings.property_binder.delegates.AttachedForeignDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ParentDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.RelationDelegate
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO

import po.misc.callbacks.CallbackPayload
import po.misc.callbacks.callbackManager
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import po.misc.reflection.mappers.models.PropertyContainer
import po.misc.reflection.properties.toRecord


class BindingHub<DTO, D, E, F_DTO, FD, FE>(
    val hostingDTO: CommonDTO<DTO, D, E>,
    val identifiable: Identifiable = asIdentifiable(hostingDTO.sourceName, "BindingHub")
): Identifiable by identifiable
        where  DTO : ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity {
    internal data class NotificationData<DTO : ModelDTO, F_DTO : ModelDTO>(
        val delegateName: String,
        val propertyRecord: PropertyContainer<Any>,
        val identifiable: Identifiable,
        val delegate: DelegateInterface<DTO, F_DTO>
    )

    internal data class ListData<DTO, D, E>(
        val hostingDTO: CommonDTO<DTO, D, E>,
        val delegates: List<DelegateInterface<DTO, *>>
    ) where  DTO : ModelDTO, D : DataModel, E : LongEntity

    enum class Event(override val value: Int) : ValueBased {
        DelegateRegistered(10),
        DelegateRegistrationComplete(11)
    }

    val dtoClass: DTOBase<DTO, D, E> get() = hostingDTO.dtoClass
    val daoService: DAOService<DTO, D, E> get() = hostingDTO.daoService
    val dtoFactory: DTOFactory<DTO, D, E> get() = hostingDTO.dtoFactory

    internal val notifier = callbackManager<NotificationData<DTO, *>>()
    internal val listNotifier = callbackManager<ListData<DTO, D, E>>()

    private val responsiveDelegates: MutableMap<String, ResponsiveDelegate<DTO, D, E, *>> = mutableMapOf()
    private val attachedForeignDelegates: MutableMap<String, AttachedForeignDelegate<DTO, D, E, *, *, *>> =
        mutableMapOf()
    private val parentDelegates: MutableMap<String, ParentDelegate<DTO, D, E, *, *, *>> = mutableMapOf()
    private val relationDelegates: MutableMap<String, RelationDelegate<DTO, D, E, F_DTO, FD, FE, *>> = mutableMapOf()

    init {
        dtoFactory.notifier.subscribe(this, CallbackPayload.create(DTOFactory.Events.OnCreated, ::onDtoInitialized))
    }

    private fun setId(delegate: DelegateInterface<DTO, *>, mapSize: Int) {
        delegate.module.setId(mapSize + 1)
    }

    fun onDtoInitialized(dto: CommonDTO<DTO, D, E>) {
        listNotifier.triggerForAll(Event.DelegateRegistrationComplete, ListData(dto, combinedList()))
    }

    private fun combinedList(): List<DelegateInterface<DTO, *>> {
        val result: MutableList<DelegateInterface<DTO, *>> = mutableListOf()
        result.addAll(responsiveDelegates.values)
        result.addAll(attachedForeignDelegates.values)
        result.addAll(parentDelegates.values)
        result.addAll(relationDelegates.values)
        return result.toList()
    }

    fun setRelationBinding(
        delegate: RelationDelegate<DTO, D, E, F_DTO, FD, FE, *>
    ): RelationDelegate<DTO, D, E, F_DTO, FD, FE, *> {
        setId(delegate, relationDelegates.size)
        relationDelegates[delegate.module.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        val notificationData =
            NotificationData(delegate.module.completeName, delegate.property.toRecord(), delegate.module, delegate)
        notifier.triggerForAll(Event.DelegateRegistered, notificationData)
        return delegate
    }


    fun setParentDelegate(
        delegate: ParentDelegate<DTO, D, E, *, *, *>
    ): ParentDelegate<DTO, D, E, *, *, *> {
        setId(delegate, parentDelegates.size)
        parentDelegates[delegate.module.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        val notificationData =
            NotificationData(delegate.module.completeName, delegate.property.toRecord(), delegate.module, delegate)
        notifier.triggerForAll(Event.DelegateRegistered, notificationData)
        return delegate
    }

    fun setAttachedForeignDelegate(
        delegate: AttachedForeignDelegate<DTO, D, E, *, *, *>
    ): AttachedForeignDelegate<DTO, D, E, *, *, *> {
        setId(delegate, parentDelegates.size)
        attachedForeignDelegates[delegate.module.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        val notificationData =
            NotificationData(delegate.module.completeName, delegate.property.toRecord(), delegate.module, delegate)
        notifier.triggerForAll(Event.DelegateRegistered, notificationData)
        return delegate
    }


    fun setResponsiveDelegate(
        delegate: ResponsiveDelegate<DTO, D, E, *>
    ): ResponsiveDelegate<DTO, D, E, *> {
        setId(delegate as DelegateInterface<DTO, *>, responsiveDelegates.size)
        responsiveDelegates[delegate.module.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        return delegate
    }

    fun getResponsiveDelegates(): List<ResponsiveDelegate<DTO, D, E, *>> {
        return this.responsiveDelegates.values.toList()
    }

    fun getRelationDelegates(cardinality: Cardinality = Cardinality.ONE_TO_MANY): List<RelationDelegate<DTO, D, E, F_DTO, FD, FE, *>> {
        return this.relationDelegates.values.filter { it.cardinality == cardinality }.toList()
    }

    fun getAttachedForeignDelegates(): List<AttachedForeignDelegate<DTO, D, E, *, *, *>> {
        return attachedForeignDelegates.values.toList()
    }

    fun getParentDelegates(): List<ParentDelegate<DTO, D, E, *, *, *>> {
        return parentDelegates.values.toList()
    }

    fun getDelegateByComponent() {

    }

    /***
     * updateEntity update entity properties
     * relations are not yet assigned
     */
    internal fun updateEntity(entity: E) {
        responsiveDelegates.values.forEach { responsiveDelegate ->
            responsiveDelegate.updateDataProperty(hostingDTO.dataModel, entity)
        }
    }

    /***
     * createChildByData child DTO creation with parent finalized
     */
    internal fun createChildByData() {
        getRelationDelegates(hostingDTO.cardinality).forEach { relationDelegate ->
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

    fun createByEntity() {
        responsiveDelegates.values.forEach { responsiveDelegate ->
            responsiveDelegate.updateDTOProperty(hostingDTO.getEntity(hostingDTO))
        }
        getRelationDelegates(hostingDTO.cardinality).forEach { relationDelegate ->
            relationDelegate.createByEntity()
        }
    }

    fun updateFromData(data: D) {
        responsiveDelegates.values.forEach {
            it.updateDataProperty(data, hostingDTO.getEntity(hostingDTO))
        }
        getRelationDelegates(hostingDTO.cardinality).forEach { relationDelegate ->
            relationDelegate.updateFromData(data)
        }
    }
}