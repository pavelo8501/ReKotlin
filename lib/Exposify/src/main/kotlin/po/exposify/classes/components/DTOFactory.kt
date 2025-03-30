package po.exposify.classes.components

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.constructors.ConstructorBuilder
import po.exposify.constructors.DTOBlueprint
import po.exposify.constructors.DataModelBlueprint
import po.exposify.constructors.EntityBlueprint
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.dto.CommonDTO
import po.lognotify.eventhandler.EventHandler
import po.lognotify.eventhandler.interfaces.CanNotify
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf


class DTOFactory<DATA, ENTITY>(
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

    internal lateinit var dataBlueprint : DataModelBlueprint<DATA>
        private set

    private lateinit var entityBlueprint : EntityBlueprint<ENTITY>
    private val dtoBlueprint = DTOBlueprint(entityDTOClass).also { it.initialize(Companion) }

    private var dataModelConstructor : (() -> DATA)? = null

    override val eventHandler = EventHandler("Factory", parent.eventHandler)

    private var serializers  =  mapOf<String, KSerializer<*>>()

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

    fun setSerializableTypes(types: List<Pair<String, KSerializer<*> >>){
        serializers =  types.associate {
            it.first to  it.second
        }
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


    fun <PARENT_DATA: DataModel>extractDataModel(
        property: KProperty1<PARENT_DATA, DATA?>,
        owningDataModel:PARENT_DATA): DATA?{
        try {
            return  property.get(owningDataModel)
        }catch (ex: IllegalStateException){
            println(ex.message)
            return null
        }
    }

    /**
     * Create new instance of DatModel injectable to the specific DTOFunctions<DATA, ENTITY> described by generics set
     * Has an optional parameter with manually defined constructor function
     * @input constructFn : (() -> DATA)? = null
     * @return DATA
     * */
    suspend fun createDataModel(constructFn : (() -> DATA)? = null):DATA{
        try {
            constructFn?.let {
                info("DataModel created from constructor provided in the createDataModel(constructFn)")
                return it.invoke()
            }
            dataModelConstructor?.let {
                info("DataModel created from constructor provided in the dataModelConstructor")
                return it.invoke()
            }

            dataBlueprint.setExternalParamLookupFn { param ->
                var paramValue : Any? = null
                val foundSerializer =  serializers[param.name]
                if(foundSerializer != null){
                    val kClassForType =  param.type.classifier as? KClass<*>
                    if(kClassForType != null){
                       val serializer : KSerializer<*> =   when {
                           kClassForType.isSubclassOf(List::class)->{
                               val elementType = param.type.arguments.firstOrNull()?.type?.classifier as? KClass<*>
                               val elementSerializer = foundSerializer as? KSerializer<Any>
                                   ?: error("No serializer found for list element type: $elementType")

                               ListSerializer(elementSerializer)
                           }
                           else -> foundSerializer
                        }
                        paramValue = Json.decodeFromString(serializer as KSerializer<Any>, "[]")
                    }
                }
                paramValue
            }
            val args = dataBlueprint.getArgsForConstructor()
            val dataModel = task("DataModel created from dataBlueprint [reflection]") {
                val constructor = dataBlueprint.getConstructor()
                constructor.callBy(args)
            }
            return dataModel!!
        }catch (ex: Exception) {
           throw OperationsException("DataModel  creation failed ${ex.message}", ExceptionCodes.REFLECTION_ERROR)
        }
    }

    /**
     * Create new instance of  DTOFunctions
     * if input param dataModel provided use it as an injection into constructor
     * if not then create new DataModel instance with default parameters i.e. no data will be preserved
     * @input dataModel:  DATA?
     * @return DTOFunctions<DATA, ENTITY> or null
     * */
    suspend fun createEntityDto(dataModel : DATA? = null): CommonDTO<DATA, ENTITY>?{
        val model = dataModel?: createDataModel()
            val newDto = task<CommonDTO<DATA, ENTITY>>("DTOFunctions created from dtoBlueprint [reflection]") {
            dtoBlueprint.setExternalParamLookupFn { param ->
                when (param.name) {
                    "dataModel" -> {
                        model
                    }
                    else -> {
                        null
                    }
                }
            }
            val args = dtoBlueprint.getArgsForConstructor()
            dtoBlueprint.getConstructor().callBy(args)
        }
        newDto?.initialize()?: println("Something wrong")
        return newDto
    }
}
