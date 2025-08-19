package po.exposify.dto.components.bindings

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.bindings.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.AttachedForeignDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.ComplexDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.ParentDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.RelationDelegate
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.helpers.warnIfNull
import po.exposify.dto.helpers.warning
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.extensions.castOrOperations

import po.exposify.extensions.getOrOperations
import po.lognotify.TasksManaged
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.types.safeCast



data class RelationDelegateKey<DTO, D, E>(
    val dtoClass: DTOBase<DTO, D, E>,
    val cardinality: Cardinality
) where DTO : ModelDTO, D : DataModel, E : LongEntity


data class RelationDelegatesContainer<DTO, D, E>(
    val hostingDTO: CommonDTO<DTO, D, E>,

) where DTO : ModelDTO, D : DataModel, E : LongEntity{
    val delegates   = mutableListOf<RelationDelegate<DTO, D, E, *, *, *, *>>()
}



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
    internal val hostingFactory: DTOFactory<DTO, D, E> get() = hostingDTO.dtoFactory
    internal val hostingDAO: DAOService<DTO, D, E> get() = hostingDTO.daoService

    internal val tracker: DTOTracker<DTO, D, E>
        get() {
            return hostingDTO.tracker
        }

    internal val responsiveDelegateMap: MutableMap<String, ResponsiveDelegate<DTO, D, E, *>> = mutableMapOf()
    internal val attachedForeignMap: MutableMap<DTOBase<*, *, *>, AttachedForeignDelegate<DTO, D, E, *, *, *>> =
        mutableMapOf()

    internal val parentDelegateMap: MutableMap<DTOBase<*, *, *>, ParentDelegate<DTO, D, E, *, *, *>> = mutableMapOf()
    internal val relationDelegateMap: MutableMap<RelationDelegateKey<*, *, *>, RelationDelegate<DTO, D, E, *, *, *, *>> = mutableMapOf()


    internal val dtoStateDump: String
        get() {
            val parameters =
                responsiveDelegateMap.values.joinToString(separator = "; ", postfix = " ]") {
                    it.toString()
                }
            return "[ id = ${hostingDTO.id} $parameters"
        }

    internal val dtoRelationDump: String get() {
        var resultString = ""
        relationDelegateMap.forEach {(key, value)->
            resultString +=  " ${value.property.name}[${key.cardinality.name}](${value.commonDTOS.size})"
        }
        return resultString
    }



    internal fun <F : ModelDTO, FD : DataModel, FE : LongEntity> getParentDelegateByType(
        dtoType: CommonDTOType<F, FD, FE>,
    ): ParentDelegate<DTO, D, E, F, FD, FE>? {

        val delegate = parentDelegateMap.values.firstOrNull { it.commonDTOType == dtoType }
        delegate.warnIfNull("No parent delegate found for type: ${dtoType.dtoType.typeName}", this)
        return delegate?.safeCast()
    }


    internal fun <F : ModelDTO, FD : DataModel, FE : LongEntity> getAttachedForeignDelegateByType(
        dtoType: CommonDTOType<F, FD, FE>,
    ): AttachedForeignDelegate<DTO, D, E, F, FD, FE>? {
        val delegate = attachedForeignMap.values.firstOrNull { it.commonDTOType == dtoType }
        delegate.warnIfNull("No parent delegate found for type: ${dtoType.dtoType.typeName}", this)
        return delegate?.safeCast()
    }

    internal fun <F : ModelDTO, FD : DataModel, FE : LongEntity> getRelationDelegateByType(
        dtoType: CommonDTOType<F, FD, FE>,
    ): RelationDelegate<DTO, D, E, F, FD, FE, *>? {
        val delegate = relationDelegateMap.values.firstOrNull { it.hostingDTOClass.commonDTOType == dtoType }
        delegate.warnIfNull("No relation delegate found for type: ${dtoType.dtoType.typeName}", this)
        return delegate?.safeCast()
    }
    internal fun <F : ModelDTO, FD : DataModel, FE : LongEntity> getRelationByTypeThrowing(
        dtoType: CommonDTOType<F, FD, FE>,
    ): RelationDelegate<DTO, D, E, F, FD, FE, *> = getRelationDelegateByType(dtoType).getOrOperations(this)


    internal fun <F : ModelDTO, FD : DataModel, FE : LongEntity> registerRelationDelegate(
        delegate: RelationDelegate<DTO, D, E, F, FD, FE, *>,
    ): RelationDelegate<DTO, D, E, F, FD, FE, *> {
        relationDelegateMap[RelationDelegateKey(delegate.dtoClass, delegate.cardinality)] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        return delegate
    }

    internal fun  <F : ModelDTO, FD : DataModel, FE : LongEntity> registerComplexDelegate(
        delegate: ComplexDelegate<DTO, D, E,  F, FD, FE>,
    ): ComplexDelegate<DTO, D, E,  F, FD, FE> {

        when (delegate) {
            is ParentDelegate -> {
                parentDelegateMap[delegate.dtoClass] = delegate
                delegate.updateStatus(DelegateStatus.Registered)
            }

            is AttachedForeignDelegate -> {
                attachedForeignMap[delegate.dtoClass] = delegate
                delegate.updateStatus(DelegateStatus.Registered)
            }
        }
        return delegate
    }

    internal fun registerResponsiveDelegate(
        delegate: ResponsiveDelegate<DTO, D, E, *>

    ): ResponsiveDelegate<DTO, D, E, *> {

        delegate.identity.setId(responsiveDelegateMap.values.size.toLong() + 1)
        responsiveDelegateMap[delegate.completeName] = delegate
        delegate.updateStatus(DelegateStatus.Registered)
        return delegate
    }


    internal fun  getRelationDelegatesPacked(
    ): RelationDelegatesContainer<DTO, D, E>{
        val pack = RelationDelegatesContainer(hostingDTO)
        relationDelegateMap.values.forEach {
            pack.delegates.add(it)
        }
        return pack
    }


    internal fun <F : ModelDTO, FD : DataModel, FE : LongEntity> getRelationDelegates(
        dtoClass: DTOBase<F, FD, FE>,
    ): List<RelationDelegate<DTO, D, E, F, FD, FE, *>>{

        if(dtoClass == hostingDTO.dtoClass){
            warning("getRelationDelegates by $dtoClass on dto of class ${hostingDTO.dtoClass}")
        }

        val result = mutableListOf<RelationDelegate<DTO, D, E, F, FD, FE, *>>()
        val oneToOne =  relationDelegateMap[RelationDelegateKey(dtoClass, Cardinality.ONE_TO_ONE)]
        if(oneToOne != null){
            result.add(oneToOne.castOrOperations(this))
        }
        val oneToMany =  relationDelegateMap[RelationDelegateKey(dtoClass, Cardinality.ONE_TO_MANY)]
        if(oneToMany != null){
            result.add(oneToMany.castOrOperations(this))
        }
        return result
    }

    internal fun updateByData(dataModel: D) {
        responsiveDelegateMap.values.forEach { responsiveDelegate ->
            responsiveDelegate.updateBy(this, dataModel)
        }
    }

    internal fun updateByEntity(entity: E) {
        responsiveDelegateMap.values.forEach { responsiveDelegate ->
            responsiveDelegate.updateBy(this, entity)
        }
        attachedForeignMap.values.forEach { attachedForeignDelegate->
            attachedForeignDelegate.updateDataModel(this)
        }
        hostingDTO.entityContainer.provideValue(entity)
    }

    internal fun updateEntity(entity: E) {
        responsiveDelegateMap.values.forEach { responsiveDelegate ->
            responsiveDelegate.updateEntity(this, entity)
        }
        attachedForeignMap.values.forEach {attachedForeignDelegate->
            attachedForeignDelegate.updateEntity(this, entity)
        }
        parentDelegateMap.values.forEach {parentDelegate->
            parentDelegate.updateEntity(this, entity)
        }
    }

    internal fun <F : ModelDTO, FD : DataModel, FE : LongEntity> resolveParent(
        commonDTO: CommonDTO<F, FD, FE>
    ){
        val found = parentDelegateMap[commonDTO.dtoClass]
        found?.castOrOperations<ParentDelegate<DTO, D, E, F, FD, FE>>(this)?.resolve(commonDTO) ?:run {
            warning("No parent delegate found for: ${commonDTO.dtoClass}")
        }
    }

    internal fun resolveAttachedForeign(
        callingContext: CTX,
        dataModel: D
    ){
        attachedForeignMap.values.forEach {attachedForeignDelegate->
            attachedForeignDelegate.resolve(dataModel)
        }
    }

    internal fun resolveAttachedForeign(
        callingContext: CTX,
        entity: E
    ){
        attachedForeignMap.values.forEach {attachedForeignDelegate->
            attachedForeignDelegate.resolve(entity)
        }
    }

    internal fun <F: ModelDTO, FD: DataModel, FE: LongEntity> resolveAttachedForeign(
        callingContext: CTX,
        commonDTO: CommonDTO<F, FD, FE>
    ) {
        val found =  attachedForeignMap[commonDTO.dtoClass]
        found?.castOrOperations<AttachedForeignDelegate<DTO, D, E, F, FD, FE>>(callingContext)?.resolve(commonDTO) ?:run {
            warning("No relation delegate found for type: ${commonDTO.dtoClass}")
        }
    }
}

