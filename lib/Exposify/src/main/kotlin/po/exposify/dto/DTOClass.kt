package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.DatabaseManager
import po.exposify.common.classes.exposifyDebugger
import po.exposify.common.events.ContextData
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dao.transaction.withTransactionIfNone
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.DTOConfiguration
import po.exposify.dto.components.executioncontext.RootExecutionContext
import po.exposify.dto.components.bindings.helpers.shallowDTO
import po.exposify.dto.configuration.setupValidation
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.interfaces.ClassDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.initException
import po.exposify.extensions.getOrInit
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.lognotify.TasksManaged
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.processors.SeverityLevel
import po.misc.functions.registries.NotifierRegistry
import po.misc.functions.registries.builders.notifierRegistryOf
import po.misc.interfaces.ValueBased
import po.misc.validators.models.CheckStatus

sealed class DTOBase<DTO, D, E>(
    internal val commonDTOType: CommonDTOType<DTO, D, E>,
    val dtoConfiguration: DTOConfig<DTO, D, E> = DTOConfig(commonDTOType),
) : DTOConfiguration<DTO, D, E> by dtoConfiguration,
    ClassDTO,
    TasksManaged where DTO : ModelDTO, D : DataModel, E : LongEntity {

    enum class Events(
        override val value: Int,
    ) : ValueBased {
        Initialized(1),
        StatusChanged(2),
        NewHierarchyMember(3),
    }

    abstract override val identity: CTXIdentity<out CTX>

    internal val onStatusChanged : NotifierRegistry<DTOBase<DTO, D, E>> by lazy{ notifierRegistryOf(Events.StatusChanged) }
    internal val onInitialized : NotifierRegistry<DTOBase<DTO, D, E>> by lazy{ notifierRegistryOf(Events.Initialized) }
    internal val onNewMember : NotifierRegistry<DTOBase<*, *, *>> by lazy { notifierRegistryOf(Events.NewHierarchyMember) }

    var status: DTOClassStatus = DTOClassStatus.Uninitialized
        private set

    internal val debugger =
        exposifyDebugger(this, ContextData) {
            ContextData(it.message)
        }

    val entityClass: ExposifyEntityClass<E> get() = commonDTOType.entityType.entityClass

    abstract val serviceClass: ServiceClass<*, *, *>

    init {
        withTransactionIfNone(debugger, false) {
            commonDTOType.initializeColumnMetadata()
        }
        val thisDTO = this

    }

    abstract fun setup()

    @PublishedApi
    internal fun updateStatus(newStatus: DTOClassStatus) {
        this.notify("$this Changed status from ${status.name} to ${newStatus.name}", SeverityLevel.INFO)

        status = newStatus

        onStatusChanged.trigger(this)
        if (newStatus == DTOClassStatus.Initialized) {
            onInitialized.trigger(this)
        }
    }

    @PublishedApi
    internal fun initializationComplete(validationResult: CheckStatus) {
        if (validationResult == CheckStatus.PASSED) {
            updateStatus(DTOClassStatus.Initialized)
        } else {
            val root = findHierarchyRoot()
            DatabaseManager.signalCloseConnection(this, root.serviceClass.connectionClass)
            finalize()
            throw initException("DTO validation check failure", ExceptionCode.BAD_DTO_SETUP, this)
        }
    }

    internal fun finalize() {
        notify("Finalizing", SeverityLevel.WARNING)
        if(this is RootDTO) {
            clearCachedDTOs()
        }
        updateStatus(DTOClassStatus.Uninitialized)
        dtoConfiguration.childClasses.values.forEach { it.finalize() }
    }

    override fun getAssociatedTables(cumulativeList: MutableList<IdTable<Long>>) {
        cumulativeList.add(entityClass.table)
        dtoConfiguration.childClasses.values.forEach {
            it.getAssociatedTables(cumulativeList)
        }
    }

    @PublishedApi
    internal fun initialization(): DTOBase<DTO, D, E> {
        if (status == DTOClassStatus.Uninitialized) {
            setup()
        }
        return this
    }


    fun findHierarchyRoot(): RootDTO<*, *, *> =
        when (this) {
            is RootDTO -> this
            is DTOClass -> parentClass.findHierarchyRoot()
        }

    override fun toString(): String = identity.identifiedByName
}

abstract class RootDTO<DTO, DATA, ENTITY>(
    commonType: CommonDTOType<DTO, DATA, ENTITY>,
) : DTOBase<DTO, DATA, ENTITY>(commonType),
    ClassDTO
    where DTO : ModelDTO, DTO : CTX, DATA : DataModel, ENTITY : LongEntity {
    override val identity: CTXIdentity<RootDTO<DTO, DATA, ENTITY>> = asIdentity()

    private var serviceContextParameter: ServiceContext<DTO, DATA, ENTITY>? = null

    @PublishedApi
    internal val serviceContext: ServiceContext<DTO, DATA, ENTITY>
        get() = serviceContextParameter.getOrInit(this)

    override val serviceClass: ServiceClass<DTO, DATA, ENTITY>
        get() = serviceContext.serviceClass

    internal val executionContext: RootExecutionContext<DTO, DATA, ENTITY> by lazy {
        RootExecutionContext(this)
    }

    init {
        identity.setNamePattern { "${it.className}[${commonType.dtoType.simpleName}]" }
    }



    internal fun runValidation() {
        updateStatus(DTOClassStatus.PreFlightCheck)
        setupValidation(shallowDTO())
    }

    internal fun initialization(serviceContext: ServiceContext<DTO, DATA, ENTITY>) {
        serviceContextParameter = serviceContext
        if (status == DTOClassStatus.Uninitialized) {
            notify("Launching initialization sequence", SeverityLevel.INFO)
            setup()
        }
    }
    fun clearCachedDTOs(){
        executionContext.dtoList.clear()
    }
}

abstract class DTOClass<DTO, DATA, ENTITY>(
    commonType: CommonDTOType<DTO, DATA, ENTITY>,
    val parentClass: DTOBase<*, *, *>,
) : DTOBase<DTO, DATA, ENTITY>(commonType),
    ClassDTO,
    TasksManaged where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity {
    override val identity: CTXIdentity<DTOClass<DTO, DATA, ENTITY>> = asIdentity()

    override val serviceClass: ServiceClass<*, *, *> get() = findHierarchyRoot().serviceClass

    init {
        identity.setNamePattern { "${it.className}[${commonType.dtoType.simpleName}]" }
    }



    internal fun <F : ModelDTO, FD : DataModel, FE : LongEntity> runValidation(parentDTO: CommonDTO<F, FD, FE>) {
        updateStatus(DTOClassStatus.PreFlightCheck)
        setupValidation(parentDTO.shallowDTO(this))
    }

}
