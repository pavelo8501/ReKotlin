package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.DAOService
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.DataModelContainer
import po.exposify.dto.components.MultipleRepository
import po.exposify.dto.components.RepositoryBase
import po.exposify.dto.components.SingleRepository
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.dto.enums.DTOInitStatus
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.interfaces.ComponentType
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.castOrOperationsEx
import po.misc.collections.CompositeEnumKey
import po.misc.collections.generateKey
import po.misc.registries.type.TypeRegistry
import po.misc.types.castOrThrow
import po.misc.types.safeCast

abstract class CommonDTO<DTO, DATA, ENTITY>(
   val dtoClass: DTOBase<DTO, DATA, ENTITY>
): ModelDTO where DTO : ModelDTO,  DATA: DataModel , ENTITY: LongEntity {

    val dtoClassConfig: DTOConfig<DTO, DATA, ENTITY>
        get() {
           return dtoClass.config.castOrOperationsEx<DTOConfig<DTO, DATA, ENTITY>>("dtoClassConfig uninitialized")
        }

    val registryRecord : TypeRegistry get() {
       return dtoClass.config.registry
    }

    override val dtoName : String get() = "CommonDTO[${registryRecord.getSimpleName(ComponentType.DTO)}]"
    override val cardinality: Cardinality = Cardinality.ONE_TO_MANY

    abstract override var dataModel: DATA

    override val daoService: DAOService<DTO, DATA, ENTITY>  get() = dtoClassConfig.daoService
    override val dtoFactory: DTOFactory<DTO, DATA, ENTITY>  get() = dtoClassConfig.dtoFactory


    private var entityParameter : ENTITY? = null
    val entity : ENTITY get() = entityParameter ?:run {
        val newEntity = bindingHub.updateEntity(daoService.save(this))
        provideInsertedEntity(newEntity)
    }

    fun provideInsertedEntity(entity: ENTITY):ENTITY{
        entityParameter = entity
        dataModel.id = entity.id.value
        return entity
    }

    @PublishedApi
    internal val bindingHub : BindingHub<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity> = BindingHub(dtoClass)

//    override val dataContainer: DataModelContainer<DTO, DATA> by lazy {
//       val dataContainer = DataModelContainer<DTO, DATA>(dataModel, dtoClassConfig.dtoFactory.dataBlueprint)
//       dataContainer.onDataModelUpdated = ::dataModelContainerUpdated
//       dataContainer
//    }

    var onInitializationStatusChange : ((CommonDTO<DTO, DATA, ENTITY>)-> Unit)? = null
    var initStatus: DTOInitStatus = DTOInitStatus.UNINITIALIZED
        set(value){
            if(value!= field){
                field = value
                onInitializationStatusChange?.invoke(this)
            }
        }

    override var id : Long = 0
        get(){return dataModel.id}

    val repositories: MutableMap<CompositeEnumKey<DTOClass<*,*,*>, Cardinality>, RepositoryBase<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>>
            = mutableMapOf()

    internal var trackerParameter: DTOTracker<DTO, DATA>? = null
    override val tracker: DTOTracker<DTO, DATA>
        get(){
           return trackerParameter?: DTOTracker(this)
        }

    fun createFromData(){
        bindingHub.createFromData(this)
    }
    fun createFromEntity(): CommonDTO<DTO,DATA,ENTITY>{
        return  bindingHub.createFromEntity(this)
    }

    private fun dataModelContainerUpdated(model : DATA){
        dataModel = model
    }

//    internal fun <C_DTO:ModelDTO, CD : DataModel, CE : LongEntity> getRepository(
//        childClass: DTOClass<C_DTO, CD, CE>,
//        cardinality: Cardinality,
//    ):RepositoryBase<DTO, DATA, ENTITY, C_DTO, CD, CE>{
//        val repo = repositories[childClass.generateKey(cardinality)]
//        return repo.castOrThrow<RepositoryBase<DTO, DATA, ENTITY, C_DTO, CD, CE>, OperationsException>(
//            "Child repository not found @ $dtoName",
//            ExceptionCode.VALUE_NOT_FOUND.value
//        )
//    }

//    internal fun <C_DTO:ModelDTO, CD : DataModel, CE : LongEntity> getOneToOneRepository(
//        childClass: DTOClass<C_DTO, CD, CE>
//    ): SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>{
//        val foundRepository = repositories[childClass.generateKey(Cardinality.ONE_TO_ONE)]
//        if(foundRepository!= null){
//            return foundRepository.castOrThrow<SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>, OperationsException>()
//        }else{
//            throw OperationsException("Child repository not found @ $dtoName", ExceptionCode.REPOSITORY_NOT_FOUND)
//        }
//    }
//
//    internal fun <C_DTO: ModelDTO, CD: DataModel, CE: LongEntity> getOneToManyRepository(
//        childClass: DTOClass<C_DTO, CD, CE>
//    ): MultipleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>{
//
//        val foundRepository = repositories[childClass.generateKey(Cardinality.ONE_TO_MANY)]
//        if(foundRepository!= null){
//            return foundRepository.castOrThrow<MultipleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>, OperationsException>()
//        }else{
//            throw OperationsException("Child repository not found @ $dtoName", ExceptionCode.REPOSITORY_NOT_FOUND)
//        }
//    }
//
//    fun applyRepository(
//        cardinality : Cardinality,
//        repository: RepositoryBase<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>
//    ){
//        repositories[repository.childClass.generateKey(cardinality)] = repository
//    }
//
//    fun getDtoRepositories():List<RepositoryBase<DTO, DATA, ENTITY, ModelDTO, DataModel, LongEntity>>{
//        return repositories.values.toList()
//    }

    fun <F_DTO: ModelDTO, FD: DataModel, FE: LongEntity> getDtoRepositories(
        childClass: DTOClass<F_DTO, FD, FE>
    ):List<RepositoryBase<DTO, DATA, ENTITY, F_DTO, FD, FE>>{
        return repositories.values.filter { it.childClass == childClass }
            .mapNotNull{ it.safeCast<RepositoryBase<DTO, DATA, ENTITY, F_DTO, FD, FE>>() }
    }


    fun <P_DTO: ModelDTO, PD: DataModel, PE: LongEntity>updateBindingsAfterInserted(
        container: EntityUpdateContainer<ENTITY, P_DTO, PD, PE>
    ){
       // dataContainer.setDataModelId(container.ownEntity.id.value)
        entityParameter = container.ownEntity
        //bindingHub.afterInsertUpdate(container)
        initStatus = DTOInitStatus.INITIALIZED
    }


   internal fun initialize(tracker: DTOTracker<DTO, DATA>? = null): CommonDTO<DTO,DATA, ENTITY> {
       if(tracker != null){
           trackerParameter = tracker
       }
       return this
   }
}