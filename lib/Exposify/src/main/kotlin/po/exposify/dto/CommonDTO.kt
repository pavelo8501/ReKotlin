package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.property_binder.PropertyBinder
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.DAOService
import po.exposify.dto.interfaces.DataModel
import po.exposify.common.classes.MapBuilder
import po.exposify.dto.components.DTOConfig
import po.exposify.common.classes.repoBuilder
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.MultipleRepository
import po.exposify.dto.components.RepositoryBase
import po.exposify.dto.components.SingleRepository
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.components.property_binder.DTOPropertyBinder
import po.exposify.dto.components.relation_binder.MultipleChildContainer
import po.exposify.dto.components.relation_binder.SingleChildContainer
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTORegistryItem
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.dto.enums.DTOInitStatus
import po.exposify.dto.models.DTOTracker
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.castOrOperationsEx
import po.misc.collections.CompositeKey
import po.misc.collections.generateKey
import po.misc.types.castOrThrow

abstract class CommonDTO<DTO, DATA, ENTITY>(
   val dtoClass: DTOBase<DTO, DATA>
): ModelDTO where DTO : ModelDTO,  DATA: DataModel , ENTITY: LongEntity {

    val dtoClassConfig: DTOConfig<DTO, DATA, ENTITY>
        get() = dtoClass.config.castOrOperationsEx<DTOConfig<DTO, DATA, ENTITY>>("dtoClassConfig uninitialized")

    private val registryItem : CommonDTORegistryItem<DTO, DATA, ENTITY> by lazy {
        val regItem =  CommonDTORegistryItem(
            dtoClassConfig.registry.dataKClass,
            dtoClassConfig.registry.entityKClass,
            dtoClassConfig.registry.commonDTOKClass,
            this)
        regItem
    }

    override val dtoName : String get() = "[CommonDTO ${registryItem.commonDTOKClass.simpleName.toString()}]"

    abstract override val dataModel: DATA

    override val daoService: DAOService<DTO, DATA, ENTITY>  get() = dtoClassConfig.daoService

    private var insertedEntity : ENTITY? = null
    val daoEntity : ENTITY
        get() {
            return  insertedEntity?:daoService.entityModel[id]
        }

    internal val dtoPropertyBinder : DTOPropertyBinder<DTO, DATA, ENTITY> = DTOPropertyBinder(this)

    override val propertyBinder : PropertyBinder<DATA, ENTITY>
        get()  {
            initStatus = DTOInitStatus.PARTIAL_WITH_DATA
            return dtoClassConfig.propertyBinder
        }

    override val dataContainer: DataModelContainer<DTO, DATA> by lazy {
        DataModelContainer(dataModel, dtoClassConfig.dtoFactory.dataBlueprint, propertyBinder)
    }

    var onInitializationStatusChange : ((CommonDTO<DTO, DATA, ENTITY>)-> Unit)? = null
    var initStatus: DTOInitStatus = DTOInitStatus.UNINITIALIZED
        set(value){
            if(value!= field){
                field = value
                onInitializationStatusChange?.invoke(this)
            }
        }

    override var id : Long = 0
        get(){return dataContainer.dataModel.id}

    internal var repositories
        : Map<CompositeKey<DTOBase<*,*>, Cardinality>, RepositoryBase<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>> = emptyMap()

    override val dtoTracker: DTOTracker<DTO, DATA> by lazy { DTOTracker(this) }

    internal fun <C_DTO:ModelDTO, CD : DataModel, CE : LongEntity> getRepository(
        key: CompositeKey<DTOBase<*,*>, Cardinality>
    ):RepositoryBase<DTO, DATA, ENTITY, C_DTO, CD, CE>{
        return repositories[key].castOrThrow<RepositoryBase<DTO, DATA, ENTITY, C_DTO, CD, CE>, OperationsException>(
            "Child repository not found @ $dtoName",
            ExceptionCode.VALUE_NOT_FOUND.value
        )
    }

    internal fun <C_DTO:ModelDTO, CD : DataModel, CE : LongEntity> getOneToOneRepository(
        childClass: DTOClass<C_DTO, CD>
    ): SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>{

        val foundRepository = repositories[childClass.generateKey(Cardinality.ONE_TO_ONE)]
        if(foundRepository!= null){
            return foundRepository.castOrThrow<SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>, OperationsException>()
        }else{
            throw OperationsException("Child repository not found @ $dtoName", ExceptionCode.REPOSITORY_NOT_FOUND)
        }
    }

    internal fun <C_DTO: ModelDTO, CD: DataModel, CE: LongEntity> getOneToManyRepository(
        childClass: DTOClass<*, *>
    ): MultipleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>{

        val foundRepository = repositories[childClass.generateKey(Cardinality.ONE_TO_MANY)]
        if(foundRepository!= null){
            return foundRepository.castOrThrow<MultipleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>, OperationsException>()
        }else{
            throw OperationsException("Child repository not found @ $dtoName", ExceptionCode.REPOSITORY_NOT_FOUND)
        }
    }

    fun <CHILD_DTO: ModelDTO, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity> createRepository(
        binding : MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    ): MultipleRepository<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
    {
        val newRepo =  MultipleRepository(binding, this, binding.childConfig)
        repositories = repoBuilder(
            dtoClass.generateKey(Cardinality.ONE_TO_MANY),
            newRepo.castOrOperationsEx<MultipleRepository<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>>(),
            repositories )
        return newRepo
    }

    @JvmName("createRepositorySingle")
    fun <CHILD_DTO: ModelDTO, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity> createRepository(
        binding : SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    ): SingleRepository<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
    {
        val newRepo = SingleRepository(binding, this, binding.childConfig)
        repositories = repoBuilder(
            dtoClass.generateKey(Cardinality.ONE_TO_ONE),
            newRepo.castOrOperationsEx<MultipleRepository<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>>(),
            repositories )
        return newRepo
    }

    fun getDtoRepositories():List<RepositoryBase<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>>{
        return repositories.values.toList()
    }

    suspend fun <P_DTO: ModelDTO, PD: DataModel, PE: LongEntity>updateBindingsAfterInserted(
        container: EntityUpdateContainer<ENTITY, P_DTO, PD, PE>
    ){
        dataContainer.setDataModelId(container.ownEntity.id.value)
        insertedEntity = container.ownEntity
        dtoPropertyBinder.afterInsertUpdate(container)
        initStatus = DTOInitStatus.INITIALIZED
    }

    suspend fun <P_DTO: ModelDTO, PD: DataModel, PE: LongEntity> updatePropertyBinding(
        entity : ENTITY,
        updateMode: UpdateMode,
        container: EntityUpdateContainer<ENTITY, P_DTO, PD, PE>)
    : CommonDTO<DTO ,DATA, ENTITY>
    {
        propertyBinder.update(dataContainer.dataModel, entity, updateMode)
        dtoPropertyBinder.beforeInsertUpdate(container, updateMode)
        if(updateMode == UpdateMode.ENTITY_TO_MODEL || updateMode == UpdateMode.ENTITY_TO_MODEL_FORCED){
            updateBindingsAfterInserted(container)
        }
        initStatus = DTOInitStatus.INITIALIZED
        return this
    }

   internal fun initialize() { selfRegistration(registryItem) }

    companion object{
        val dtoRegistry: MapBuilder<String, CommonDTORegistryItem<*,*,*>> = MapBuilder<String, CommonDTORegistryItem<*,*,*>>()
        internal fun <DTO: ModelDTO, DATA :DataModel, ENTITY: LongEntity> selfRegistration(
            regItem :  CommonDTORegistryItem<DTO, DATA, ENTITY>
        ){
            dtoRegistry.putIfAbsent(regItem.typeKeyCombined, regItem)
        }
    }
}