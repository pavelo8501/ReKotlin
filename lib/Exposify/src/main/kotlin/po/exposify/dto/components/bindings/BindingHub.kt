package po.exposify.dto.components.bindings

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
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
import po.exposify.extensions.castOrOperations
import po.lognotify.classes.action.InlineAction
import po.lognotify.classes.action.runInlineAction
import po.misc.callbacks.manager.builders.bridgeFrom
import po.misc.callbacks.manager.builders.callbackBuilder
import po.misc.callbacks.manager.builders.createPayload
import po.misc.callbacks.manager.builders.managerHooks
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import po.misc.interfaces.asIdentifiableClass
import po.misc.lookups.HierarchyNode
import po.misc.reflection.mappers.models.PropertyContainer
import po.misc.reflection.properties.models.PropertyUpdate
import po.misc.reflection.properties.toRecord
import po.misc.types.TypeRecord
import po.misc.types.castOrManaged


class BindingHub<DTO, D, E>(
    val hostingDTO: CommonDTO<DTO, D, E>,
    val identifiable: Identifiable = asIdentifiable(hostingDTO.sourceName, "BindingHub")
): IdentifiableClass, InlineAction
        where  DTO : ModelDTO, D: DataModel, E: LongEntity
{
    internal data class NotificationData<DTO : ModelDTO,D:DataModel, E: LongEntity,  F_DTO : ModelDTO>(
        val self: BindingHub<DTO, D, E>,
        val delegateName: String,
        val propertyRecord: PropertyContainer<Any>,
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

    override val identity = asIdentifiableClass("DTOFactory", hostingDTO.sourceName)

    internal val notifier = callbackBuilder<Event> {
        createPayload<Event, NotificationData<DTO,D,E, *>>(Event.DelegateRegistered)
        createPayload<Event,ListData<DTO, D, E>>(Event.DelegateRegistrationComplete){
            bridgeFrom(dtoClass.delegateRegistrationForward)
        }
    }


    val dtoClass: DTOBase<DTO, D, E> get() = hostingDTO.dtoClass
    val daoService: DAOService<DTO, D, E> get() = hostingDTO.daoService
    val dtoFactory: DTOFactory<DTO, D, E> get() = hostingDTO.dtoFactory

    private val responsiveDelegates: MutableMap<String, ResponsiveDelegate<DTO, D, E, *>> = mutableMapOf()
    private val attachedForeignDelegates: MutableMap<String, AttachedForeignDelegate<DTO, D, E, *, *, *>> =
        mutableMapOf()
    private val parentDelegates: MutableMap<String, ParentDelegate<DTO, D, E, *, *, *>> = mutableMapOf()
    private val relationDelegates: MutableMap<String, RelationDelegate<DTO, D, E, *, *, *, *>> = mutableMapOf()

    init {
        notifier.managerHooks{
            newSubscription {
                println(it)
            }
            beforeTrigger {
                println(it)
            }
        }

        dtoFactory.onCreatedPayload.subscribe(this){
            onDtoInitialized(it.getData())
        }
    }

    fun getDtoInfo(): List<PropertyUpdate<*>>{
        TODO("Not yet implemented")
    }

    fun resolveHierarchy(): HierarchyNode<CommonDTO<*, *, *>> {
        fun traverse(dtos: List<CommonDTO<*, *, *>>): List<HierarchyNode<CommonDTO<*, *, *>>> {
            return dtos.groupBy { it.dtoType }
                .map { (type, group) ->
                    val children = group
                        .flatMap { it.bindingHub.relationDelegates.values.flatMap { delegate -> delegate.getChildDTOs() } }
                    HierarchyNode(type, group.toMutableList()).addChildNodes(traverse(children))
                }
        }
        val rootNode = HierarchyNode<CommonDTO<*, *, *>>(hostingDTO.dtoType, mutableListOf(hostingDTO))
        val firstLevelChildren = hostingDTO.bindingHub.relationDelegates.values
            .flatMap { it.getChildDTOs() }
        rootNode.children += traverse(firstLevelChildren)
        return rootNode
    }

    fun onDtoInitialized(dto: CommonDTO<DTO, D, E>) {
        dtoClass.delegateRegistrationForward.triggerForAll(ListData(dto,  combinedList()))
    }

    private fun combinedList(): List<DelegateInterface<DTO, *>> {
        val result: MutableList<DelegateInterface<DTO, *>> = mutableListOf()
        result.addAll(responsiveDelegates.values)
        result.addAll(attachedForeignDelegates.values)
        result.addAll(parentDelegates.values)
        result.addAll(relationDelegates.values)
        return result.toList()
    }

    fun <F_DTO: ModelDTO> setRelationBinding(
        delegate: RelationDelegate<DTO, D, E, F_DTO, *, *, *>
    ): RelationDelegate<DTO, D, E, *, *, *, *> {
        delegate.identity.provideId(relationDelegates.size.toLong() + 1)
        relationDelegates[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        val notificationData =
            NotificationData(this, delegate.completeName, delegate.property.toRecord(), delegate as  DelegateInterface<DTO, F_DTO>)

        notifier.trigger(Event.DelegateRegistered, notificationData)

        return delegate
    }


    fun <F_DTO: ModelDTO> setParentDelegate(
        delegate: ParentDelegate<DTO, D, E, F_DTO, *, *>
    ): ParentDelegate<DTO, D, E, F_DTO, *, *> {
        delegate.identity.provideId(parentDelegates.size.toLong() + 1)
        parentDelegates[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        val notificationData =
            NotificationData(this, delegate.completeName, delegate.property.toRecord(), delegate.castOrOperations<DelegateInterface<DTO, F_DTO>>(this))
        notifier.trigger(Event.DelegateRegistered, notificationData)
        return delegate
    }

    fun <F_DTO: ModelDTO> setAttachedForeignDelegate(
        delegate: AttachedForeignDelegate<DTO, D, E, F_DTO, *, *>
    ): AttachedForeignDelegate<DTO, D, E, *, *, *> {
        delegate.identity.provideId(attachedForeignDelegates.size.toLong() + 1)
        attachedForeignDelegates[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        val notificationData =
            NotificationData(this, delegate.completeName, delegate.property.toRecord(), delegate.castOrOperations<DelegateInterface<DTO, F_DTO>>(this))
        notifier.trigger(Event.DelegateRegistered, notificationData)
        return delegate
    }


    fun setResponsiveDelegate(
        delegate: ResponsiveDelegate<DTO, D, E, *>
    ): ResponsiveDelegate<DTO, D, E, *> {
        delegate.identity.provideId(responsiveDelegates.size.toLong() + 1)
        responsiveDelegates[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        return delegate
    }

    fun getResponsiveDelegates(): List<ResponsiveDelegate<DTO, D, E, *>> {
        return this.responsiveDelegates.values.toList()
    }

    fun getRelationDelegates(cardinality: Cardinality = Cardinality.ONE_TO_MANY): List<RelationDelegate<DTO, D, E, *, *, *, *>> {
        return this.relationDelegates.values.filter { it.cardinality == cardinality }.toList()
    }

    fun getAttachedForeignDelegates(): List<AttachedForeignDelegate<DTO, D, E, *, *, *>> {
        return attachedForeignDelegates.values.toList()
    }

    fun getParentDelegates(): List<ParentDelegate<DTO, D, E, *, *, *>> {
        return parentDelegates.values.toList()
    }


    internal fun update(entity: E) = runInlineAction("update") {
        responsiveDelegates.values.forEach { responsiveDelegate ->
            responsiveDelegate.update(entity)
        }
        attachedForeignDelegates.values.forEach {attached->
            attached.resolveForeign(hostingDTO.dataModel, entity)
        }
    }


    internal fun update(data: D){
        responsiveDelegates.values.forEach { responsiveDelegate ->
            responsiveDelegate.updateBy(data)
        }
        getRelationDelegates(hostingDTO.cardinality).forEach {delegate->
            delegate.updateBy(data)
        }
    }

    internal fun create(){
        if(dtoClass is RootDTO){
            val newEntity =  daoService.save {
                update(it)
            }
            hostingDTO.provideEntity(newEntity)
            dtoClass.registerDTO(hostingDTO)

            val relationDelegates = getRelationDelegates(Cardinality.ONE_TO_MANY)
            relationDelegates.forEach {delegateC->
                delegateC.updateBy(hostingDTO.dataModel)
            }
        }else{
            val relationDelegates = getRelationDelegates(hostingDTO.cardinality)
            relationDelegates.forEach {
                it.updateBy(hostingDTO.dataModel)
            }
        }
    }

    internal fun select(entity:E){
        hostingDTO.provideEntity(entity)
        responsiveDelegates.values.forEach {
            it.updateBy(entity)
        }
        attachedForeignDelegates.values.forEach {attachedForeign->
            attachedForeign.resolveForeign(entity)
        }
        val delegates = if (dtoClass is RootDTO) {
            getRelationDelegates(Cardinality.ONE_TO_MANY)
        } else {
            getRelationDelegates(hostingDTO.cardinality)
        }
        delegates.forEach {
            it.createByEntity(entity)
        }
    }

    internal fun <F_DTO: ModelDTO, FD : DataModel, FE: LongEntity>  setParent(parentDTO: CommonDTO<F_DTO, FD, FE>){
        parentDelegates.forEach { (key, value)->
           if(value.foreignClass == parentDTO.dtoClass){
               value.resolveForeign(parentDTO)
           }
        }
    }
}