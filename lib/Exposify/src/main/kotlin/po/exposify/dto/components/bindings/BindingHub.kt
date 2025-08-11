package po.exposify.dto.components.bindings

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.bindings.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.AttachedForeignDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.ComplexDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.ParentDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.RelationDelegate
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.enums.DTOStatus
import po.exposify.dto.helpers.warnIfNull
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.dto.models.ForeignDataModels
import po.exposify.dto.models.ForeignEntities
import po.exposify.extensions.castOrOperations
import po.lognotify.TasksManaged
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.types.TypeData
import po.misc.types.containers.TypedContainer
import po.misc.types.safeCast
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
    val hostingDTO: CommonDTO<DTO, D, E>,
) : TasksManaged where DTO : ModelDTO, D : DataModel, E : LongEntity {

    override val identity: CTXIdentity<BindingHub<DTO, D, E>> = asIdentity()

    val commonDTOType: CommonDTOType<DTO, D, E> get() = hostingDTO.commonType

    private val dtoClass: DTOBase<DTO, D, E> get() = hostingDTO.dtoClass
    private val daoService: DAOService<DTO, D, E> get() = hostingDTO.daoService
    private val dtoFactory: DTOFactory<DTO, D, E> get() = hostingDTO.dtoFactory

    internal val tracker: DTOTracker<DTO, D, E> get() {
        return hostingDTO.tracker
    }

    private val responsiveDelegateMap: MutableMap<String, ResponsiveDelegate<DTO, D, E, *>> = mutableMapOf()
    internal val responsiveDelegates: List<ResponsiveDelegate<DTO, D, E, *>> get() = responsiveDelegateMap.values.toList()

    private val attachedForeignDelegateMap: MutableMap<String, AttachedForeignDelegate<DTO, D, E, *, *, *>> = mutableMapOf()

    internal val attachedForeignDelegates: List<AttachedForeignDelegate<DTO, D, E, *, *, *>> get() =
        attachedForeignDelegateMap.values.toList()

    private val containerizedParentMap: MutableMap<TypeData<*>, TypedContainer<*>> = mutableMapOf()
    private val parentDelegateMap: MutableMap<CTXIdentity<out CTX>, ParentDelegate<DTO, D, E, *, *, *>> = mutableMapOf()
    internal val parentDelegates: List<ParentDelegate<DTO, D, E, *, *, *>> get() = parentDelegateMap.values.toList()
    private val initializedParentDelegates: List<ParentDelegate<DTO, D, E, *, *, *>> get() = parentDelegates.filter { it.initialized }

    private val relationDelegateMap: MutableMap<String, RelationDelegate<DTO, D, E, *, *, *>> = mutableMapOf()

    @PublishedApi
    internal val relationDelegates: List<RelationDelegate<DTO, D, E, *, *, *>> get() = relationDelegateMap.values.toList()

    private val childBindingHubs: List<BindingHub<*, *, *>> get() = relationDelegates.flatMap { it.childBindingHubs }

    internal val dtoStateDump: String get() {
        val parameters =
            responsiveDelegates.joinToString(separator = "; ", postfix = " ]") {
                it.toString()
            }
        return "[ id = ${hostingDTO.id} $parameters"
    }

    data class ParentParameters<DTO, D, E>(
        val bindingHub: BindingHub<DTO, D, E>,
        val commonType: CommonDTOType<DTO, D, E>,
        val entity: E,
    ) where DTO : ModelDTO, D : DataModel, E : LongEntity

    private fun createParameters(entity: E): ParentParameters<DTO, D, E> = ParentParameters(this, commonDTOType, entity)

//    private fun resolveParent(entity: E){
//        parentDelegates.forEach {
//            it.resolve(entity)
//        }
//    }

    internal fun <F : ModelDTO, FD : DataModel, FE : LongEntity> getParentDelegateByType(
        dtoType: CommonDTOType<F, FD, FE>,
    ): ParentDelegate<DTO, D, E, F, FD, FE>? {
        val delegate = parentDelegates.firstOrNull { it.foreignCommonDTOType == dtoType }
        delegate.warnIfNull("No parent delegate found for type: ${dtoType.dtoType.typeName}", this)
        return delegate?.safeCast()
    }


    internal fun <F : ModelDTO, FD : DataModel, FE : LongEntity> getAttachedForeignDelegateByType(
        dtoType: CommonDTOType<F, FD, FE>,
    ): AttachedForeignDelegate<DTO, D, E, F, FD, FE>? {
        val delegate = attachedForeignDelegates.firstOrNull { it.foreignCommonDTOType == dtoType }
        delegate.warnIfNull("No parent delegate found for type: ${dtoType.dtoType.typeName}", this)
        return delegate?.safeCast()
    }

    internal fun <F : ModelDTO, FD : DataModel, FE : LongEntity> getRelationDelegateByType(
        dtoType: CommonDTOType<F, FD, FE>,
    ): RelationDelegate<DTO, D, E, F, FD, FE>? {

        val delegate = relationDelegates.firstOrNull { it.foreignClass.commonDTOType == dtoType }
        delegate.warnIfNull("No relation delegate found for type: ${dtoType.dtoType.typeName}", this)
        return delegate?.safeCast()
    }

    internal fun updateEntity(entity: E) {
        responsiveDelegates.forEach { responsiveDelegate ->
            responsiveDelegate.updateEntityWithPropertyValues(entity)
        }
    }

    internal fun updateByEntity(entity: E) {
        responsiveDelegates.forEach { responsiveDelegate ->
            responsiveDelegate.updateBy(entity)
        }
    }

    private fun updateData() {
        tracker.logDebug("updateData", this)
        responsiveDelegates.forEach { responsiveDelegate ->
            responsiveDelegate.updateData()
        }
        attachedForeignDelegates.forEach {
            it.update()
        }
    }

    internal fun <F : ModelDTO> registerRelationDelegate(
        delegate: RelationDelegate<DTO, D, E, F, *, *>,
    ): RelationDelegate<DTO, D, E, *, *, *> {
        delegate.identity.setId(relationDelegateMap.size.toLong() + 1)
        relationDelegateMap[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        return delegate
    }

    internal fun <F : ModelDTO> registerComplexDelegate(
        delegate: ComplexDelegate<DTO, D, E, F, *, *>,
    ): ComplexDelegate<DTO, D, E, F, *, *> {
        when (delegate) {
            is ParentDelegate -> {
                parentDelegateMap[delegate.identity] = delegate
                delegate.updateStatus(DelegateStatus.Registered)
            }
            is AttachedForeignDelegate -> {
                delegate.identity.setId(attachedForeignDelegates.size.toLong() + 1)

                val complete = delegate.completeName

                attachedForeignDelegateMap[delegate.completeName] = delegate
                delegate.updateStatus(DelegateStatus.Registered)
            }
        }
        return delegate
    }

    internal fun registerResponsiveDelegate(delegate: ResponsiveDelegate<DTO, D, E, *>): ResponsiveDelegate<DTO, D, E, *> {
        delegate.identity.setId(responsiveDelegates.size.toLong() + 1)
        responsiveDelegateMap[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        return delegate
    }

    fun getRelationDelegates(cardinality: Cardinality): List<RelationDelegate<DTO, D, E, *, *, *>> =
        relationDelegateMap.values
            .filter {
                it.cardinality == cardinality
            }.toList()

    /**
     * Populates responsive property delegates with values from the given [entity].
     * @return The updated [hostingDTO] reference.
     */
    internal fun updatePropertiesBy(entity: E): CommonDTO<DTO, D, E> {
        responsiveDelegates.forEach { responsiveDelegate ->
            responsiveDelegate.updateBy(entity)
        }
        return hostingDTO
    }

    internal fun updateBy(dataModel: D, withEntity:E? = null): CommonDTO<DTO, D, E> {
        responsiveDelegates.forEach { responsiveDelegate ->
            responsiveDelegate.updateBy(dataModel, withEntity)
        }
        return hostingDTO
    }

    internal fun <F: ModelDTO, FD: DataModel, FE: LongEntity> resolveParent(commonDTO: CommonDTO<F, FD, FE>){
        getParentDelegateByType(commonDTO.commonType)?.resolveParent(commonDTO) ?:run {
            notify("Delegate not found for $commonDTO")
        }
    }

    internal fun resolveAttachedForeign(entity: E){
        attachedForeignDelegates.forEach { it.resolveForeign(entity) }
    }


    internal fun resolveAttachedForeign(dataModel: D, entityToUpdate:E? = null) {
        attachedForeignDelegates.forEach { it.resolveForeign(dataModel, entityToUpdate) }
    }

    internal fun <F: ModelDTO, FD: DataModel, FE: LongEntity> lookUpChild(
        id: Long,
        typeData: CommonDTOType<F, FD, FE>
    ): CommonDTO<F, FD, FE>?{
        val delegate = getRelationDelegateByType(typeData)
        return delegate?.childDTOS?.firstOrNull { it.id == id}
    }

    /**
     * Performs a full update cycle for this DTO and all related children.
     * If [hostingDTO] has data but no entity, a new one is created and provided to child DTOs via entity binders.
     * If it has an entity but no data, the data is updated and all relation delegates are attached.
     * @param foreign Optional foreign parameters to provide parent context to children.
     */
    internal fun <F : ModelDTO, FD : DataModel, FE : LongEntity> updateDTOs(foreign: ParentParameters<F, FD, FE>?) {
        when (hostingDTO.dtoStatus) {
            DTOStatus.PartialWithData -> {
                val thisEntity =
                    daoService.save { newEntity ->
                        hostingDTO.entityContainer.provideValue(newEntity)
                        updateEntity(newEntity)
                        if (foreign != null) {
                            initializedParentDelegates.forEach { delegate ->
                                tracker.logDebug("Providing entity for binding", this)
                                delegate.entityBinder.provideValue(newEntity)
                            }
                        }
                    }
                thisEntity.flush()
                hostingDTO.entityContainer.provideValue(thisEntity)
                // hostingDTO.provideEntity(thisEntity)
                childBindingHubs.forEach { childBindingHub ->
                    val params = createParameters(thisEntity)
                    childBindingHub.updateDTOs(params)
                }
                dtoClass.registerDTO(hostingDTO)
            }
            DTOStatus.PartialWithEntity -> {
                updateData()
                relationDelegates.forEach { relation ->
                    relation.childBindingHubs.map { it.updateDTOs<F, FD, FE>(null) }
                    relation.attachChildDataModel()
                }
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
    internal fun resolveHierarchy(data: D) {
         resolveAttachedForeign(data)
        if (!hostingDTO.dataContainer.isValueAvailable) {
            hostingDTO.dataContainer.provideValue(data)
        }
        updateBy(data)
        relationDelegates.forEach { relation ->
            relation.createForeignDTOS(data) { existentDto ->
                CrudOperation.Update
            }
        }
    }

    /**
     * Resolves the full DTO hierarchy using the provided [entity] as the root.
     * - Sets up an empty data model for the DTO.
     * - Resolves foreign keys and parent references.
     * - Updates responsive properties.
     * - Builds child DTOs via relation delegates.
     */
    internal fun resolveHierarchy(entity: E) {
        val emptyDataModel = dtoFactory.createDataModel()
        hostingDTO.dataContainer.provideValue(emptyDataModel)
        resolveAttachedForeign(entity)
        // resolveParent(entity)
        updatePropertiesBy(entity)
        relationDelegates.forEach { relation ->
            relation.createForeignDTOS(entity) { existentDto ->
                CrudOperation.Update
            }
        }
        hostingDTO.entityContainer.provideValue(entity)
    }

    fun <F: ModelDTO, FD: DataModel, FE: LongEntity> createDTOS(
        foreignCommonDTOType: CommonDTOType<F, FD, FE>,
        dataModels: List<FD>
    ): List<CommonDTO<F, FD, FE>>{
        val result = mutableListOf<CommonDTO<F, FD, FE>>()
        val delegate = getRelationDelegateByType(foreignCommonDTOType).castOrOperations<RelationDelegate<DTO, D, E, F, FD, FE>>(this)
        dataModels.forEach {
           val newCommonDTO = delegate.createForeignDTO(it){
                CrudOperation.Create
            }
            result.add(newCommonDTO)
        }
        return result
    }

    internal fun extractChildData(data: D): List<ForeignDataModels<*, *, *>>{
        val result = mutableListOf<ForeignDataModels<*, *, *>>()
        relationDelegates.forEach {delegate->
            delegate.extractChildData(data)?.let {
                result.add(it)
            }
        }
        return result
    }

    internal fun extractChildEntities(entity: E): List<ForeignEntities<*, *, *>>{
        val result = mutableListOf<ForeignEntities<*, *, *>>()
        relationDelegates.forEach {delegate->
            delegate.extractChildEntities(entity)?.let {
                result.add(it)
            }
        }
        return result
    }

    internal fun <F: ModelDTO, FD: DataModel, FE: LongEntity> attachDTOS(
        dtoList: List<CommonDTO<F, FD, FE>>,
        typeData: CommonDTOType<F, FD, FE>
    ){
       val delegate =  getRelationDelegateByType(typeData)
        delegate.warnIfNull("No relation delegate found for type: ${typeData.dtoType.typeName}", this)
        delegate?.attachDTOS(dtoList)
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
    fun loadHierarchyByData(
        data: D,
        initiator: CTX,
    ): CommonDTO<DTO, D, E> {
        tracker.logDebug("loadHierarchyByData on $hostingDTO", initiator)
        resolveHierarchy(data)
        tracker.logDebug("updateDTOs on $hostingDTO initiated by${initiator.contextName}", initiator)
        updateDTOs<DTO, D, E>(null)

        return hostingDTO
    }

    fun loadHierarchyByData(initiator: CTX): CommonDTO<DTO, D, E> {
        tracker.logDebug("loadHierarchyByData on $hostingDTO", initiator)
        val data = hostingDTO.dataContainer.getValue(initiator)
        resolveHierarchy(data)
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
    fun loadHierarchyByEntity(
        entity: E,
        initiator: CTX,
    ): CommonDTO<DTO, D, E> {
        tracker.logDebug("loadHierarchyByEntity on $hostingDTO", initiator)
        resolveHierarchy(entity)
        tracker.logDebug("updateDTOs on $hostingDTO initiated by${initiator.contextName}", initiator)
        updateDTOs<DTO, D, E>(null)
        return hostingDTO
    }
}
