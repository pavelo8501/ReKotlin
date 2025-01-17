package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.components.eventhandler.EventHandler
import po.db.data_service.components.eventhandler.interfaces.CanNotify
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.constructors.DTOBlueprint
import po.db.data_service.constructors.DataModelBlueprint
import po.db.data_service.constructors.EntityBlueprint
import po.db.data_service.controls.Notificator
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.CommonDTO
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class Factory<DATA, ENTITY>(
   val parent: DTOClass<DATA,ENTITY>,
   val entityDTOClass : KClass<out CommonDTO<DATA, ENTITY>>
): CanNotify where DATA: DataModel,   ENTITY: LongEntity   {
    companion object : ConstructorBuilder()

    private var _dataModelClass: KClass<DATA>? = null
    val dataModelClass: KClass<DATA>
        get(){return _dataModelClass?: throw OperationsException(
            "DataModel Class uninitialized", ExceptionCodes.LAZY_NOT_INITIALIZED) }

    private var _daoEntityClass: KClass<ENTITY>? = null
    val daoEntityClass: KClass<ENTITY>
        get(){return _daoEntityClass?: throw OperationsException(
            "DataModel Class uninitialized", ExceptionCodes.LAZY_NOT_INITIALIZED) }

    private var dataBlueprint : DataModelBlueprint<DATA>? = null
    private lateinit var entityBlueprint : EntityBlueprint<ENTITY>
    private val dtoBlueprint = DTOBlueprint(entityDTOClass).also { it.initialize(Companion) }

    private var dataModelConstructor : (() -> DATA)? = null

    override val eventHandler : EventHandler = EventHandler("Factory", parent.eventHandler)

    /**
     * Initializes the blueprints for DataModel, Entity, and DTO based on the provided classes.
     *  @input dataClazz: KClass<DATA>
     *  @input entityClass : KClass<ENTITY>
     */
    fun initializeBlueprints(
        dataClazz : KClass<DATA>,
        entityClass : KClass<ENTITY>,
    ){
        _dataModelClass = dataClazz
        _daoEntityClass = entityClass

        dataBlueprint =   DataModelBlueprint(dataModelClass).also { it.initialize(Companion) }
        entityBlueprint = EntityBlueprint(daoEntityClass).also { it.initialize(Companion) }
    }

    fun setDataModelConstructor(dataModelConstructor : (() -> DATA)){
        this.dataModelConstructor = dataModelConstructor
    }


    /**
     * Extracts a list of child data models from a parent data model based on the specified property.
     *
     * This function uses a given property (a `KProperty1`) to access an iterable collection of child
     * data models (`DATA`) from a parent data model (`PARENT_DATA`). It converts the iterable collection
     * into a `List` for further processing.
     *
     * @param PARENT_DATA The type of the parent data model.
     * @param DATA The type of the child data model.
     * @param property The property of the parent data model that holds the iterable collection of child data models.
     * @param owningDataModel The instance of the parent data model from which to extract the child data models.
     * @return A `List` of child data models extracted from the parent data model.
     *
     * @throws IllegalStateException If the property accessor is inaccessible or improperly configured.
     *
     * @sample
     * ```
     * data class Parent(val children: List<Child>)
     * data class Child(val name: String)
     *
     * val parent = Parent(listOf(Child("Alice"), Child("Bob")))
     * val children = extractDataModel(Parent::children, parent)
     * println(children) // Output: [Child(name=Alice), Child(name=Bob)]
     * ```
     */
    fun <PARENT_DATA: DataModel>extractDataModel(
        property: KProperty1<PARENT_DATA, Iterable<DATA>>,
        owningDataModel:PARENT_DATA): List<DATA>{
        try {
            return  property.get(owningDataModel).toList()
        }catch (ex: IllegalStateException){
            println(ex.message)
            return emptyList()
        }
    }

    /**
     * Create new instance of DatModel injectable to the specific CommonDTO<DATA, ENTITY> described by generics set
     * Has an optional parameter with manually defined constructor function
     * @input constructFn : (() -> DATA)? = null
     * @return DATA
     * */
    fun createDataModel(constructFn : (() -> DATA)? = null):DATA{
        try{
            constructFn?.let {
                notify("DataModel created from constructor provided in the createDataModel(constructFn)")
                return  it.invoke()
            }
            dataModelConstructor?.let {
                notify("DataModel created from constructor provided in the dataModelConstructor")
                return it.invoke()
            }

            dataBlueprint?.let {
               val dataModel = notify("DataModel created from dataBlueprint [reflection]"){
                    val constructor =  it.getConstructor()
                    constructor.callBy(it.getArgsForConstructor())
                }
                return dataModel!!
            }?:run {
                TODO("Extract DATA blueprint from entityDTOClass as a reserve fallback")
            }
        } catch (ex: Exception) {
            throw OperationsException("DataModel  creation failed ${ex.message}", ExceptionCodes.REFLECTION_ERROR)
        }
    }

    /**
     * Create new instance of  CommonDTO
     * if input param dataModel provided use it as an injection into constructor
     * if not then create new DataModel instance with default parameters i.e. no data will be preserved
     * @input dataModel:  DATA?
     * @return CommonDTO<DATA, ENTITY> or null
     * */
    fun createEntityDto(dataModel : DATA? = null): CommonDTO<DATA, ENTITY>?{
        val model = dataModel?: createDataModel()
        try {
            val dto = notify<CommonDTO<DATA, ENTITY>>("CommonDTO created from dtoBlueprint [reflection]") {
                dtoBlueprint.let { blueprint ->
                    val constructor = blueprint.getConstructor()
                    blueprint.getArgsForConstructor { paramName ->
                        when (paramName) {
                            "dataModel" -> {
                                model
                            }

                            else -> {
                                null
                            }
                        }
                    }.let {
                        val newDto = constructor.callBy(it)
                        newDto
                    }
                }
            }
            return dto
        }catch (ex: Exception){
            notifyError(ex.message?:"Unknown exception")
            return null
        }
    }
}
