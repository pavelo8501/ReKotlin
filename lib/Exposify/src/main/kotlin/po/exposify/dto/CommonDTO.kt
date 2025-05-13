package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.DAOService
import po.exposify.dto.interfaces.DataModel
import po.exposify.common.classes.MapBuilder
import po.exposify.dto.components.DTOConfig
import po.exposify.common.classes.repoBuilder
import po.exposify.dto.components.DTOFactory
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
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.dto.enums.DTOInitStatus
import po.exposify.dto.models.DTORegistryItem
import po.exposify.dto.models.DTOTracker
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.castOrOperationsEx
import po.misc.collections.CompositeKey
import po.misc.collections.generateKey
import po.misc.types.castOrThrow
import po.misc.types.safeCast

abstract class CommonDTO<DTO, DATA, ENTITY>(
   val dtoClass: DTOBase<DTO, DATA, ENTITY>
): ModelDTO where DTO : ModelDTO,  DATA: DataModel , ENTITY: LongEntity {

    val dtoClassConfig: DTOConfig<DTO, DATA, ENTITY>
        get() = dtoClass.config.castOrOperationsEx<DTOConfig<DTO, DATA, ENTITY>>("dtoClassConfig uninitialized")

    private val registryRecord : DTORegistryItem<DTO, DATA, ENTITY> get() = dtoClass.config.registryRecord

    override val dtoName : String get() = "[CommonDTO ${registryRecord.derivedDTOClazz.simpleName.toString()}]"

    abstract override var dataModel: DATA


    override val daoService: DAOService<DTO, DATA, ENTITY>  get() = dtoClassConfig.daoService
    override val dtoFactory: DTOFactory<DTO, DATA, ENTITY>  get() = dtoClassConfig.dtoFactory

    private var insertedEntity : ENTITY? = null
    val daoEntity : ENTITY
        get() {
            return  insertedEntity?:daoService.entityModel[id]
        }

    @PublishedApi
    internal val dtoPropertyBinder : DTOPropertyBinder<DTO, DATA, ENTITY> = DTOPropertyBinder(this)

    override val dataContainer: DataModelContainer<DTO, DATA> by lazy {
       val dataContainer = DataModelContainer<DTO, DATA>(dataModel, dtoClassConfig.dtoFactory.dataBlueprint)
       dataContainer.onDataModelUpdated = ::dataModelContainerUpdated
       dataContainer
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

//    internal var repositories
//        : Map<CompositeKey<DTOBase<*, *, *>, Cardinality>, RepositoryBase<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>> = emptyMap()

    val repositories: MutableMap<CompositeKey<DTOClass<*,*,*>, Cardinality>, RepositoryBase<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>>
        = mutableMapOf()

    override val dtoTracker: DTOTracker<DTO, DATA> by lazy { DTOTracker(this) }


    private fun dataModelContainerUpdated(model : DATA){
        dataModel = model
    }

    internal fun <C_DTO:ModelDTO, CD : DataModel, CE : LongEntity> getRepository(
        childClass: DTOClass<C_DTO, CD, CE>,
        cardinality: Cardinality,
    ):RepositoryBase<DTO, DATA, ENTITY, C_DTO, CD, CE>{

        val repo = repositories[childClass.generateKey(cardinality)]

        return repo.castOrThrow<RepositoryBase<DTO, DATA, ENTITY, C_DTO, CD, CE>, OperationsException>(
            "Child repository not found @ $dtoName",
            ExceptionCode.VALUE_NOT_FOUND.value
        )
    }

    internal fun <C_DTO:ModelDTO, CD : DataModel, CE : LongEntity> getOneToOneRepository(
        childClass: DTOClass<C_DTO, CD, CE>
    ): SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>{

        val foundRepository = repositories[childClass.generateKey(Cardinality.ONE_TO_ONE)]
        if(foundRepository!= null){
            return foundRepository.castOrThrow<SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>, OperationsException>()
        }else{
            throw OperationsException("Child repository not found @ $dtoName", ExceptionCode.REPOSITORY_NOT_FOUND)
        }
    }

    internal fun <C_DTO: ModelDTO, CD: DataModel, CE: LongEntity> getOneToManyRepository(
        childClass: DTOClass<C_DTO, CD, CE>
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
        val newRepo =  MultipleRepository(binding, this, binding.childClass)
        val casted =  newRepo.castOrOperationsEx<MultipleRepository<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>>()
        repositories[binding.childClass.generateKey(Cardinality.ONE_TO_MANY)] = casted
        return newRepo
    }

    @JvmName("createRepositorySingle")
    fun <CHILD_DTO: ModelDTO, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity> createRepository(
        binding : SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    ): SingleRepository<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
    {
        val newRepo = SingleRepository(binding, this, binding.childClass)
        val casted =  newRepo.castOrOperationsEx<MultipleRepository<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>>()
        repositories[binding.childClass.generateKey(Cardinality.ONE_TO_ONE)] = casted
        return newRepo
    }

    fun getDtoRepositories():List<RepositoryBase<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>>{
        return repositories.values.toList()
    }

    fun <F_DTO: ModelDTO, FD: DataModel, FE: LongEntity> getDtoRepositories(
        childClass: DTOClass<F_DTO, FD, FE>
    ):List<RepositoryBase<DTO, DATA, ENTITY, F_DTO, FD, FE>>{
        return repositories.values.filter { it.childClass == childClass }
            .mapNotNull{ it.safeCast<RepositoryBase<DTO, DATA, ENTITY, F_DTO, FD, FE>>() }
    }

//    internal fun <F_DTO: ModelDTO, FD: DataModel, FE: LongEntity> executionProvider(
//        childClass: DTOClass<F_DTO, FD, FE>
//    ) :ClassExecutionProvider<DTO, DATA, ENTITY, F_DTO, FD, FE>{
//        return ClassExecutionProvider(this, childClass)
//    }


//    suspend fun <P_DTO: ModelDTO, PD: DataModel, PE: LongEntity>updateBindingsBeforeInserted(
//        container: EntityUpdateContainer<ENTITY, P_DTO, PD, PE>,
//    ){
////        if(container.updateMode == UpdateMode.ENTITY_TO_MODEL || container.updateMode == UpdateMode.ENTITY_TO_MODEL_FORCED){
////            updateBindingsAfterInserted(container)
////        }
//        dtoPropertyBinder.update(container)
//        initStatus = DTOInitStatus.PARTIAL_WITH_DATA
//    }

    suspend fun <P_DTO: ModelDTO, PD: DataModel, PE: LongEntity>updateBindingsAfterInserted(
        container: EntityUpdateContainer<ENTITY, P_DTO, PD, PE>
    ){
        dataContainer.setDataModelId(container.ownEntity.id.value)
        insertedEntity = container.ownEntity
        dtoPropertyBinder.afterInsertUpdate(container)
        initStatus = DTOInitStatus.INITIALIZED
    }

//    suspend fun  updatePropertyBinding(
//        updateMode: UpdateMode,
//        entity:ENTITY,
//        dataModel:DATA? = null,
//    ): CommonDTO<DTO ,DATA, ENTITY>
//    {
//        if(dataModel != null){
//            dataContainer.updateDataModel(dataModel)
//        }
//        propertyBinder.update(dataContainer.dataModel, entity, updateMode)
//        initStatus = DTOInitStatus.INITIALIZED
//        return this
//    }

   internal fun initialize() { selfRegistration(registryRecord) }

    companion object{
        val dtoRegistry: MapBuilder<String, DTORegistryItem<*,*,*>> = MapBuilder<String, DTORegistryItem<*,*,*>>()
        internal fun <DTO: ModelDTO, DATA :DataModel, ENTITY: LongEntity> selfRegistration(
            regItem :  DTORegistryItem<DTO, DATA, ENTITY>
        ){
            dtoRegistry.putIfAbsent(regItem.typeKeyCombined, regItem)
        }
    }
}