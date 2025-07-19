package po.exposify.dto.components.bindings

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.bindings.property_binder.delegates.AttachedForeignDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ParentDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.RelationDelegate
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.enums.DTOStatus
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrOperations
import po.lognotify.action.InlineAction
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.interfaces.ClassIdentity
import po.misc.context.CtxId
import po.misc.context.IdentifiableClass
import po.misc.types.TypeData
import po.misc.types.containers.TypedContainer
import po.misc.types.containers.toTypeContainer
import po.misc.types.containers.updatable.ActionValue
import kotlin.collections.flatMap

/**
 * The BindingHub acts as the central mediator between a DTO and its properties, relationships, and parent bindings.
 *
 * It provides mechanisms for:
 * - syncing entity and data model values into responsive delegates
 * - assigning parent DTOs via matching `ParentDelegate`s
 * - updating related child DTOs
 * - initializing full DTO hierarchies from either data models or entities
 *
 * This class is designed to support recursive DTO tree building in both directions
 * (from data -> entity and from entity -> data) and coordinate parent/child relationships.
 */
class BindingHub<DTO, D, E>(
    val hostingDTO: CommonDTO<DTO, D, E>
): IdentifiableClass, InlineAction where  DTO : ModelDTO, D: DataModel, E: LongEntity {

    override val identity: ClassIdentity by lazy { ClassIdentity.create("BindingHub", hostingDTO.sourceName) }

    private val dtoClass: DTOBase<DTO, D, E> get() = hostingDTO.dtoClass
    private val daoService: DAOService<DTO, D, E> get() = hostingDTO.daoService
    private val dtoFactory: DTOFactory<DTO, D, E> get() = hostingDTO.dtoFactory

    private val exPayload: ManagedCallSitePayload = ManagedCallSitePayload(this)

    internal val tracker: DTOTracker<DTO, D, E> get() {
        return hostingDTO.tracker
    }

    private val responsiveDelegateMap: MutableMap<String, ResponsiveDelegate<DTO, D, E, *>> = mutableMapOf()
    internal val responsiveDelegates: List<ResponsiveDelegate<DTO, D, E, *>> get() = responsiveDelegateMap.values.toList()

    private val attachedForeignDelegateMap: MutableMap<String, AttachedForeignDelegate<DTO, D, E, *, *, *>> = mutableMapOf()

    internal val attachedForeignDelegates: List<AttachedForeignDelegate<DTO, D, E, *, *, *>> get()=attachedForeignDelegateMap.values.toList()

    private val containerizedParentMap: MutableMap<TypeData<*>,  TypedContainer<*>> = mutableMapOf()
    private val parentDelegateMap: MutableMap<String, ParentDelegate<DTO, D, E, *, *, *>> = mutableMapOf()
    internal val parentDelegates:List<ParentDelegate<DTO, D, E, *, *, *>> get() = parentDelegateMap.values.toList()
    private val initializedParentDelegates:List<ParentDelegate<DTO, D, E, *, *, *>> get() = parentDelegates.filter { it.initialized }

    private val relationDelegateMap: MutableMap<String, RelationDelegate<DTO, D, E, *, *, *>> = mutableMapOf()

    @PublishedApi
    internal val relationDelegates: List<RelationDelegate<DTO, D, E, *, *, *>> get() = relationDelegateMap.values.toList()
    private val childBindingHubs: List<BindingHub<*,*,*>> get() = relationDelegates.flatMap { it.childBindingHubs }

    data class ParentParameters<DTO, D, E>(
        val bindingHub: BindingHub<DTO, D, E>,
        val commonType: TypeData<CommonDTO<DTO, D, E>>,
        val entity: E
    ) where  DTO : ModelDTO, D : DataModel, E : LongEntity

    private fun createParameters(entity: E): ParentParameters<DTO, D, E> {
        return ParentParameters(this, hostingDTO.commonType, entity)
    }

    private fun resolveAttachedForeign(dataModel:D):D{
        tracker.logDebug("Resolving AttachedForeign[By DataModel]", this)
        attachedForeignDelegates.forEach { it.resolveForeign(dataModel) }
        return dataModel
    }
    private fun resolveAttachedForeign(entity: E):D{
        tracker.logDebug("Resolving AttachedForeign[By Entity]", this)
        attachedForeignDelegates.forEach { it.resolveForeign(entity) }
        return hostingDTO.dataContainer.source
    }
    private fun resolveParent(entity: E){
        parentDelegates.forEach {
            it.resolve(entity)
        }
    }

    private fun updateEntity(entity:E){
        tracker.logDebug("updateEntity", this)
        responsiveDelegates.forEach { responsiveDelegate ->
            responsiveDelegate.update(entity)
        }
        attachedForeignDelegates.forEach {
            it.update(entity)
        }
    }
    private fun updateData(){
        tracker.logDebug("updateData", this)
        responsiveDelegates.forEach {responsiveDelegate->
            responsiveDelegate.updateData()
        }
        attachedForeignDelegates.forEach {
            it.update()
        }
    }

    internal fun <F : ModelDTO> registerRelationDelegate(
        delegate: RelationDelegate<DTO, D, E, F, *, *>
    ): RelationDelegate<DTO, D, E, *, *, *> {
        delegate.identity.provideId(relationDelegateMap.size.toLong() + 1)
        relationDelegateMap[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        return delegate
    }

    internal fun <F : ModelDTO> registerParentDelegate(
        delegate: ParentDelegate<DTO, D, E, F, *, *>
    ): ParentDelegate<DTO, D, E, F, *, *> {
        parentDelegateMap[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        return delegate
    }

    internal fun <F : ModelDTO> registerAttachedForeignDelegate(
        delegate: AttachedForeignDelegate<DTO, D, E, F, *, *>
    ): AttachedForeignDelegate<DTO, D, E, *, *, *> {
        delegate.identity.provideId(attachedForeignDelegates.size.toLong() + 1)
        attachedForeignDelegateMap[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        return delegate
    }

    internal fun registerResponsiveDelegate(
        delegate: ResponsiveDelegate<DTO, D, E, *>
    ): ResponsiveDelegate<DTO, D, E, *> {
        delegate.identity.provideId(responsiveDelegates.size.toLong() + 1)
        responsiveDelegateMap[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        return delegate
    }

    fun getRelationDelegates(cardinality: Cardinality): List<RelationDelegate<DTO, D, E, *, *, *>> {
       return relationDelegateMap.values.filter { it.cardinality == cardinality }.toList()
    }

    /**
     * Populates responsive property delegates with values from the given [entity].
     * @return The updated [hostingDTO] reference.
     */
    internal fun updatePropertiesBy(entity:E):CommonDTO<DTO, D, E>{
        responsiveDelegates.forEach { responsiveDelegate ->
            responsiveDelegate.updateBy(entity)
        }
        return hostingDTO
    }
    /**
     * Populates responsive property delegates with values from the given [dataModel].
     * @return The updated [hostingDTO] reference.
     */
    internal fun updatePropertiesBy(dataModel:D):CommonDTO<DTO, D, E>{
        responsiveDelegates.forEach { responsiveDelegate ->
            responsiveDelegate.updateBy(dataModel)
        }
        return hostingDTO
    }

    /**
     * Assigns a parent DTO instance to the current DTO via the matching [ParentDelegate] based on [typeData].
     * Also registers an [entityBinder] used to pass the generated entity back during persistence.
     * @param dto The parent DTO to bind to this DTO.
     * @param typeData The type descriptor of the parent DTO.
     * @param entityBinder A binding reference used to provide the entity when available.
     */
    internal fun <F: ModelDTO> assignParent(
        dto:F,
        typeData: TypeData<F>,
        entityBinder: ActionValue<E>
    ) {
        val delegate = parentDelegates.firstOrNull { it.foreignDTOType == typeData }
        if (delegate != null) {
            val casted = delegate.castOrOperations<ParentDelegate<DTO, D, E, F, *, *>>(this)
            casted.assignEntityBinder(entityBinder)
            casted.assignParentDTO(dto)
            val container = dto.toTypeContainer(typeData)
            containerizedParentMap.put(typeData, container)
        } else {
            hostingDTO.logHandler.dataProcessor.warn("AssignParent no delegates found for type ${typeData.simpleName}")
        }
    }

    /**
     * Performs a full update cycle for this DTO and all related children.
     * If [hostingDTO] has data but no entity, a new one is created and provided to child DTOs via entity binders.
     * If it has an entity but no data, the data is updated and all relation delegates are attached.
     * @param foreign Optional foreign parameters to provide parent context to children.
     */
    internal fun <F: ModelDTO, FD: DataModel, FE: LongEntity> updateDTOs(foreign: ParentParameters<F, FD, FE>?){
        when(hostingDTO.status){
            DTOStatus.PartialWithData-> {
                val thisEntity = daoService.save { newEntity ->
                    hostingDTO.entityContainer.provideSource(newEntity)
                    updateEntity(newEntity)
                    if (foreign != null) {
                        initializedParentDelegates.forEach { delegate ->
                            tracker.logDebug("Providing entity for binding", this)
                            delegate.entityBinder.provideValue(newEntity)
                        }
                    }
                }
                thisEntity.flush()
                hostingDTO.provideEntity(thisEntity, DTOStatus.Complete)
                childBindingHubs.forEach {childBindingHub->
                val params = createParameters(thisEntity)
                    childBindingHub.updateDTOs(params)
                }
                dtoClass.registerDTO(hostingDTO)
            }
            DTOStatus.PartialWithEntity->{
                updateData()
                relationDelegates.forEach {relation->
                    relation.childBindingHubs.map { it.updateDTOs<F, FD, FE>(null) }
                    relation.attachChildDataModel()
                }
                hostingDTO.updateStatus(DTOStatus.Complete)
            }
            else -> {
            }
        }
    }

    /**
     * Resolves the full DTO hierarchy using the provided [data] model as the root.
     * - Sets up data model for the current DTO.
     * - Updates responsive property delegates.
     * - Initializes child DTOs through relation delegates.
     * This prepares the system for entity creation via [updateDTOs].
     */
    internal fun resolveHierarchy(data: D){
        val updatedData =  resolveAttachedForeign(data)
        if (!hostingDTO.dataContainer.isSourceAvailable){
            hostingDTO.dataContainer.provideSource(updatedData)
        }
        updatePropertiesBy(updatedData)
        relationDelegates.forEach { relation ->
            relation.createForeignDTOS(data) { existentDto ->
                CrudOperation.Update
            }
        }
        hostingDTO.updateStatus(DTOStatus.PartialWithData)
    }

    /**
     * Resolves the full DTO hierarchy using the provided [entity] as the root.
     * - Sets up an empty data model for the DTO.
     * - Resolves foreign keys and parent references.
     * - Updates responsive properties.
     * - Builds child DTOs via relation delegates.
     */
    internal fun resolveHierarchy(entity: E){
        val emptyDataModel = dtoFactory.createDataModel()
        hostingDTO.dataContainer.provideSource(emptyDataModel)
        resolveAttachedForeign(entity)
        resolveParent(entity)
        updatePropertiesBy(entity)
        relationDelegates.forEach { relation ->
            relation.createForeignDTOS(entity) { existentDto ->
                CrudOperation.Update
            }
        }
        hostingDTO.provideEntity(entity, DTOStatus.PartialWithEntity)
    }

    /**
     * Entry point for constructing a DTO tree starting from a [data] model.
     * Internally:
     * - Initializes the DTO from data
     * - Updates the entire structure recursively
     * - Triggers entity creation for persistence
     * @param data The root data model to build from.
     * @param initiator The context triggering the process, for logging/tracking.
     * @return The updated [hostingDTO] reference.
     */
    fun loadHierarchyByData(data: D, initiator: CtxId): CommonDTO<DTO, D, E>{
        tracker.logDebug("loadHierarchyByData on $hostingDTO", initiator)
        resolveHierarchy(data)
        tracker.logDebug("updateDTOs on $hostingDTO initiated by${initiator.contextName}", initiator)
        updateDTOs<DTO, D, E>(null)
        return hostingDTO
    }

    /**
     * Entry point for constructing a DTO tree starting from an [entity].
     * Internally:
     * - Initializes the DTO from entity
     * - Updates structure recursively by resolving relationships
     * @param entity The root entity to construct the tree from.
     * @param initiator The context triggering the process, for logging/tracking.
     * @return The updated [hostingDTO] reference.
     */
    fun loadHierarchyByEntity(entity: E, initiator: CtxId): CommonDTO<DTO, D, E>{
        tracker.logDebug("loadHierarchyByEntity on $hostingDTO", initiator)
        resolveHierarchy(entity)
        tracker.logDebug("updateDTOs on $hostingDTO initiated by${initiator.contextName}", initiator)
        updateDTOs<DTO, D, E>(null)
        return hostingDTO
    }
}