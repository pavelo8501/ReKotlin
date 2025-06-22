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
import po.lognotify.classes.action.InlineAction
import po.lognotify.classes.action.runInlineAction
import po.misc.callbacks.manager.CallbackManager
import po.misc.callbacks.manager.Containable
import po.misc.callbacks.manager.builders.bridgeFrom
import po.misc.callbacks.manager.builders.callbackBuilder
import po.misc.callbacks.manager.builders.callbackManager
import po.misc.callbacks.manager.builders.createPayload
import po.misc.callbacks.manager.builders.managerHooks
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import po.misc.interfaces.asIdentifiableClass
import po.misc.reflection.mappers.models.PropertyContainer
import po.misc.reflection.properties.toRecord
import po.misc.types.castOrManaged


class BindingHub<DTO, D, E, F_DTO, FD, FE>(
    val hostingDTO: CommonDTO<DTO, D, E>,
    val identifiable: Identifiable = asIdentifiable(hostingDTO.sourceName, "BindingHub")
): IdentifiableClass, InlineAction
        where  DTO : ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{
    internal data class NotificationData<DTO : ModelDTO,D:DataModel, E: LongEntity,  F_DTO : ModelDTO>(
        val self: BindingHub<DTO, D, E, F_DTO, *, *>,
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
    private val relationDelegates: MutableMap<String, RelationDelegate<DTO, D, E, F_DTO, FD, FE, *>> = mutableMapOf()

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

    fun onDtoInitialized(dto: CommonDTO<DTO, D, E>) {
        dtoClass.delegateRegistrationForward.triggerForAll(ListData(dto,  combinedList()))

        //notifier.trigger(Event.DelegateRegistrationComplete, ListData(dto,  combinedList()))
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
        delegate.identity.provideId(relationDelegates.size+1)
        relationDelegates[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        val notificationData =
            NotificationData(this, delegate.completeName, delegate.property.toRecord(), delegate)

        notifier.trigger(Event.DelegateRegistered, notificationData)

        return delegate
    }


    fun setParentDelegate(
        delegate: ParentDelegate<DTO, D, E, *, *, *>
    ): ParentDelegate<DTO, D, E, *, *, *> {
        delegate.identity.provideId(parentDelegates.size+1)
        parentDelegates[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        val notificationData =
            NotificationData(this, delegate.completeName, delegate.property.toRecord(), delegate.castOrManaged())
        notifier.trigger(Event.DelegateRegistered, notificationData)
        return delegate
    }

    fun setAttachedForeignDelegate(
        delegate: AttachedForeignDelegate<DTO, D, E, *, *, *>
    ): AttachedForeignDelegate<DTO, D, E, *, *, *> {
        delegate.identity.provideId(attachedForeignDelegates.size+1)
        attachedForeignDelegates[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        val notificationData =
            NotificationData(this, delegate.completeName, delegate.property.toRecord(), delegate.castOrManaged())
        notifier.trigger(Event.DelegateRegistered, notificationData)
        return delegate
    }


    fun setResponsiveDelegate(
        delegate: ResponsiveDelegate<DTO, D, E, *>
    ): ResponsiveDelegate<DTO, D, E, *> {
        delegate.identity.provideId(responsiveDelegates.size+1)
        responsiveDelegates[delegate.completeName] = delegate
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


    internal fun update(entity: E) = runInlineAction("update") {
        responsiveDelegates.values.forEach { responsiveDelegate ->
            responsiveDelegate.update(entity)
        }
        attachedForeignDelegates.values.forEach {attached->
            attached.resolveForeign(hostingDTO.dataModel, entity)
        }
    }


    internal fun update(data: D) = runInlineAction("update") {
        responsiveDelegates.values.forEach { responsiveDelegate ->
            responsiveDelegate.updateBy(data)
        }
    }

    internal fun insert(entity:E) = runInlineAction("insert(entity)") {

    }

    internal fun create() = runInlineAction("create") {
        if(dtoClass is RootDTO){
            val newEntity =  daoService.save {
                update(it)
            }
            hostingDTO.provideEntity(newEntity)
            dtoClass.registerDTO(hostingDTO)
            getRelationDelegates(Cardinality.ONE_TO_MANY).forEach {relation->
                relation.update(hostingDTO.dataModel)
            }
        }else{
            getRelationDelegates(hostingDTO.cardinality).forEach { relation ->
                relation.update(hostingDTO.dataModel)
            }
        }
    }


    internal fun select(entity:E) = runInlineAction("select") {
        daoService.save {
            update(it)
            getRelationDelegates(hostingDTO.cardinality).forEach { delegate ->
                delegate.selectByEntity(entity)
            }
        }
    }

    internal fun <F_DTO: ModelDTO, FD : DataModel, FE: LongEntity>  setParent(parentDTO: CommonDTO<F_DTO, FD, FE>){
        parentDelegates.forEach { (key, value)->
           if(value.foreignClass == parentDTO.dtoClass){
               value.resolveForeign(parentDTO)
           }
        }
    }


//    internal fun createChildByData(data:D) = runInlineAction("createChildByData") {
//        getRelationDelegates(hostingDTO.cardinality).forEach { relationDelegate ->
//            relationDelegate.create()
//        }
//        attachedForeignDelegates.values.forEach {
//            it.resolveForeign(data)
//        }
//    }
//
//    internal fun <F_DTO: ModelDTO, FD : DataModel, FE: LongEntity>  setParent(parentDTO: CommonDTO<F_DTO, FD, FE>){
//        parentDelegates.forEach { (key, value)->
//           if(value.foreignClass == parentDTO.dtoClass){
//               value.resolveForeign(parentDTO)
//           }
//        }
//    }
//
//    fun createByEntity() {
//        responsiveDelegates.values.forEach { responsiveDelegate ->
//            responsiveDelegate.updateDTOProperty(hostingDTO.getEntity())
//        }
//        getRelationDelegates(hostingDTO.cardinality).forEach { relationDelegate ->
//            relationDelegate.createByEntity()
//        }
//        attachedForeignDelegates.values.forEach {
//            it.resolveForeign()
//        }
//    }
//
//    fun updateFromData(data: D) {
//        responsiveDelegates.values.forEach {
//            it.updateDataProperty(data, hostingDTO.getEntity())
//        }
//        getRelationDelegates(hostingDTO.cardinality).forEach { relationDelegate ->
//            relationDelegate.updateFromData(data)
//        }
//        attachedForeignDelegates.values.forEach {
//            it.resolveForeign(data)
//        }
//    }
}