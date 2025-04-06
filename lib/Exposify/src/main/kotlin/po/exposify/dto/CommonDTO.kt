package po.exposify.dto

import po.exposify.binders.relationship.BindingKeyBase2
import po.exposify.binders.PropertyBinder
import po.exposify.binders.UpdateMode
import po.exposify.classes.components.DAOService2
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.classes.MapBuilder
import po.exposify.classes.DTOClass
import po.exposify.dto.components.DataModelContainer2
import po.exposify.dto.components.RepositoryBase
import po.exposify.dto.enums.CrudType
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTORegistryItem
import po.exposify.dto.models.CrudOperation
import po.exposify.dto.models.DTORegistryItem
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.models.DTOInitStatus


abstract class CommonDTO<DTO, DATA, ENTITY>(
   val dtoClass: DTOClass<DTO>
):ModelDTO where DTO : ModelDTO,  DATA: DataModel , ENTITY: ExposifyEntityBase {

    var personalName : String = "unset"
    abstract val dataModel: DATA

    lateinit var daoService: DAOService2<DTO, DATA, ENTITY>
    lateinit var propertyBinderSource: PropertyBinder<DATA, ENTITY>
    val propertyBinder : PropertyBinder<DATA, ENTITY>
        get() {
            return propertyBinderSource
        }

    override lateinit var dataContainer: DataModelContainer2<DTO, DATA>

    var onInitializationStatusChange : ((CommonDTO<DTO, DATA, ENTITY>)-> Unit)? = null
    var initStatus: DTOInitStatus = DTOInitStatus.UNINITIALIZED
        set(value){
            if(value!= field){
                field = value
                onInitializationStatusChange?.invoke(this)
            }
        }
    val crudOperation : CrudOperation = CrudOperation(CrudType.NONE, true)

    override val id : Long
        get(){return dataContainer.dataModel.id}


    private var _entityDAO : ENTITY? = null
    var entityDAO : ENTITY
        set(value){  _entityDAO = value }
        get(){return  _entityDAO?:throw OperationsException(
            "Entity uninitialized",
            ExceptionCode.LAZY_NOT_INITIALIZED) }

    private var _regItem : CommonDTORegistryItem<DTO, DATA, ENTITY>? = null
    private val regItem : CommonDTORegistryItem<DTO, DATA, ENTITY>
        get(){
            return _regItem?:throw InitException("DtoClassRegistryItem uninitialized", ExceptionCode.UNDEFINED)
        }
    internal val registryItem: CommonDTORegistryItem<DTO, DATA, ENTITY> by lazy { regItem }
    internal var repositories =  MapBuilder<BindingKeyBase2,  RepositoryBase<DTO, DATA, ENTITY, ModelDTO>> ()

    init {
        val a = 10
    }

    fun compileDataModel():DATA{
        return dataModel
    }

    fun updateBinding(entity : ENTITY, updateMode: UpdateMode){
        propertyBinder.update(dataContainer.dataModel, entity, updateMode)
        entityDAO = entity
        initStatus = DTOInitStatus.INITIALIZED
    }

   internal fun initialize(
       regItem: DTORegistryItem<DTO, DATA, ENTITY>,
       container : DataModelContainer2<DTO, DATA>,
       binder: PropertyBinder<DATA,ENTITY>,
       dao : DAOService2<DTO, DATA,  ENTITY>){
       _regItem =  CommonDTORegistryItem(dtoClass, regItem.dataKClass, regItem.entityKClass, regItem.commonDTOKClass,this)
       propertyBinderSource = binder
       dataContainer = container
       daoService = dao
       dataContainer.attachBinder(propertyBinder)
       initStatus = DTOInitStatus.PARTIAL_WITH_DATA
       personalName = "${regItem.commonDTOKClass.simpleName.toString()}[CommonDTO]"
       selfRegistration(registryItem)
    }

    companion object{
        internal val dtoRegistry: MapBuilder<String, CommonDTORegistryItem<*,*,*>> = MapBuilder<String, CommonDTORegistryItem<*,*,*>>()
        internal fun <DTO: ModelDTO, DATA :DataModel, ENTITY: ExposifyEntityBase> selfRegistration(
            regItem :  CommonDTORegistryItem<DTO, DATA, ENTITY>
        ){
            dtoRegistry.putIfAbsent(regItem.typeKeyCombined, regItem)
        }
    }
}