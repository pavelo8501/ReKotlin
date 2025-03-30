package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.binder.BindingKeyBase
import po.exposify.binder.BindingKeyBase2
import po.exposify.binder.PropertyBinder
import po.exposify.binder.UpdateMode
import po.exposify.classes.components.RepositoryBase
import po.exposify.classes.components.RepositoryBase2
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.classes.ClassBlueprint
import po.exposify.common.classes.MapBuilder
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.components.DataModelContainer2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DtoClassRegistryItem2
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.InitializationException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.InitErrorCodes
import po.exposify.models.DTOInitStatus
import kotlin.reflect.KClass


abstract class CommonDTO2<DTO, DATA, ENTITY>(
    dtoClass: DTOClass2<DTO>
): DTOBase2<DTO, DATA, ENTITY, DataModel, LongEntity>(dtoClass), ModelDTO
        where DTO : ModelDTO,  DATA: DataModel , ENTITY: LongEntity {

    abstract override val dataModel: DATA

    private var _regItem : DtoClassRegistryItem2<DTO, DATA, ENTITY>? = null
    private val regItem : DtoClassRegistryItem2<DTO, DATA, ENTITY>
        get(){
            return _regItem?:throw InitializationException("DtoClassRegistryItem uninitialized", InitErrorCodes.KEY_PARAM_UNINITIALIZED)
        }
    override val registryItem: DtoClassRegistryItem2<DTO, DATA, ENTITY> by lazy { regItem }
    override val dataContainer: DataModelContainer2<DATA> = DataModelContainer2<DATA>(dataModel, dtoClass.config.dtoFactory.dataBlueprint as ClassBlueprint<DATA> , propertyBinder)

    internal var repositories =  MapBuilder<BindingKeyBase2,  RepositoryBase2<DTO, *, *, *>> ()

    init {
        val a = 10
    }

    fun compileDataModel():DATA{
        return dataModel
    }

    internal constructor(dtoClass : DTOClass2<DTO>, dataKClass:  KClass<DATA>,  entity : KClass<ENTITY>): this(dtoClass){
        val regItem = DtoClassRegistryItem2<DTO, DATA,ENTITY>(this::class, dataKClass, entity)
        _regItem =  regItem
        selfRegistration(regItem)
    }

    companion object{
        internal val dtoRegistry: MapBuilder<String, DtoClassRegistryItem2<*,*,*>> = MapBuilder<String, DtoClassRegistryItem2<*,*,*>>()

        inline operator fun <DTO: ModelDTO, reified DATA : DataModel, reified ENTITY : LongEntity>
                invoke(dtoClass: DTOClass2<DTO>): CommonDTO2<DTO, DATA, ENTITY> {
            return object : CommonDTO2<DTO, DATA, ENTITY>(dtoClass =  dtoClass, DATA::class, ENTITY::class){
                    abstract override val dataModel: DATA
            }
        }

        internal fun <DTO: ModelDTO, DATA :DataModel, ENTITY: LongEntity> selfRegistration(
            regItem :  DtoClassRegistryItem2<DTO, DATA, ENTITY>
        ){
            dtoRegistry.putIfAbsent(regItem.typeKeyCombined, regItem)
        }

    }
}

sealed class DTOBase2<DTO, DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    val dtoClass: DTOClass2<DTO>
) where DTO : ModelDTO,  DATA : DataModel, ENTITY: LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity{

    abstract val dataModel: DATA
    abstract val dataContainer : DataModelContainer2<DATA>

    val propertyBinder: PropertyBinder<DATA, ENTITY> =  dtoClass.config.propertyBinder as  PropertyBinder<DATA, ENTITY>

    var onInitializationStatusChange : ((DTOBase2<DTO, DATA, ENTITY, *, *>)-> Unit)? = null
    var initStatus: DTOInitStatus = DTOInitStatus.UNINITIALIZED
        set(value){
            if(value!= field){
                field = value
                onInitializationStatusChange?.invoke(this)
            }
        }

    var id : Long
        get(){return dataContainer.dataModel.id}
        set(value) {dataContainer.dataModel.id = value}

    private var _entityDAO : ENTITY? = null
    var entityDAO : ENTITY
        set(value){  _entityDAO = value }
        get(){return  _entityDAO?:throw OperationsException(
            "Entity uninitialized",
            ExceptionCodes.LAZY_NOT_INITIALIZED) }

    val isSaved : Boolean
        get(){
            return id != 0L
        }

    internal abstract val registryItem: DtoClassRegistryItem2<DTO, DATA, ENTITY>

    fun initialize(): DTOClass2<DTO> {
        dataContainer.attachBinder(propertyBinder)
        initStatus = DTOInitStatus.PARTIAL_WITH_DATA
        return dtoClass
    }

    fun updateBinding(entity : ENTITY, updateMode: UpdateMode){
        propertyBinder.update(dataContainer.dataModel, entity, updateMode)
        entityDAO = entity
        id =  entity.id.value
        initStatus = DTOInitStatus.INITIALIZED
    }

    fun updateBinding(dataModel: DATA, updateMode: UpdateMode){
        propertyBinder.update(dataModel, entityDAO, updateMode)
    }


}