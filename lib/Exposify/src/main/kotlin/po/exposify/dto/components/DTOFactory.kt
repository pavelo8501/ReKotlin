package po.exposify.dto.components

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import po.exposify.classes.components.DTOConfig
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.classes.ClassBlueprint
import po.exposify.common.classes.ConstructorBuilder
import po.exposify.common.classes.MapBuilder
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import kotlin.collections.get
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf

internal class DTOFactory<DTO, DATA, ENTITY>(
    private val dtoKClass : KClass<out CommonDTO<DTO, DATA, ENTITY>>,
    private val dataModelClass : KClass<DATA>,
    private val hostingConfig: DTOConfig<DTO, DATA, ENTITY>,
): TasksManaged where DTO : ModelDTO, DATA: DataModel, ENTITY: ExposifyEntityBase {
    companion object : ConstructorBuilder()

    private val personalName = "DTOFactory[${dtoKClass.simpleName}]"

    internal val dataBlueprint : ClassBlueprint<DATA> =  ClassBlueprint(dataModelClass).also { it.initialize(Companion) }
    private val dtoBlueprint = ClassBlueprint(dtoKClass).also { it.initialize(Companion) }

    private var dataModelConstructor : (() -> DATA)? = null

    init {
        val stop = 10
    }

    private var serializers  =  mapOf<String, KSerializer<*>>()
    fun setSerializableTypes(types: List<Pair<String, KSerializer<*>>>){
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

    internal var postCreationRoutines = MapBuilder<String, suspend CommonDTO<DTO, DATA, ENTITY>.(ENTITY?) -> Unit>()
    fun setPostCreationRoutine(name : String, fn: suspend CommonDTO<DTO, DATA, ENTITY>.(ENTITY?)-> Unit){
        postCreationRoutines.put(name, fn)
    }
    fun unsetPostCreationRoutine(){
        postCreationRoutines.clear()
    }


    suspend fun dtoPostCreation(dto : CommonDTO<DTO, DATA, ENTITY>, withEntity:ENTITY?){
        postCreationRoutines.map.forEach {
            println("Executing PostCreationRoutine : ${it.key} on ${dtoBlueprint.className}")
            it.value.invoke(dto, withEntity)
            println("PostCreationRoutine : ${it.key} complete")
        }
    }

    /**
     * Create new instance of DatModel injectable to the specific DTOFunctions<DATA, ENTITY> described by generics set
     * Has an optional parameter with manually defined constructor function
     * @input constructFn : (() -> DATA)? = null
     * @return DATA
     * */
    fun createDataModel(constructFn : (() -> DATA)? = null):DATA{
        try {
            constructFn?.let {
                return it.invoke()
            }
            dataModelConstructor?.let {
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
                        paramValue = Json.Default.decodeFromString(serializer as KSerializer<Any>, "[]")
                    }
                }
                paramValue
            }
            val args = dataBlueprint.getArgsForConstructor()
            val constructor = dataBlueprint.getConstructor()
            val dataModel = constructor.callBy(args)
            return dataModel
        }catch (ex: Exception) {
            throw OperationsException("DataModel  creation failed ${ex.message}", ExceptionCode.REFLECTION_ERROR)
        }
    }

    private suspend fun createDto(withDataModel : DATA?, withEntity : ENTITY?): CommonDTO<DTO, DATA, ENTITY> =
        subTask("Create DTO", personalName){
        val model = withDataModel?: createDataModel()
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
        val newDto =  dtoBlueprint.getConstructor().callBy(args)
        dtoPostCreation(newDto, withEntity)
        newDto
    }.resultOrException()

    /**
     * Create new instance of  DTOFunctions
     * if input param dataModel provided use it as an injection into constructor
     * if not then create new DataModel instance with default parameters i.e. no data will be preserved
     * @input dataModel:  DATA?
     * @return DTOFunctions<DATA, ENTITY> or null
     * */
    suspend fun createDto(dataModel : DATA? = null): CommonDTO<DTO, DATA, ENTITY>{
       return this.createDto(dataModel, null)
    }

    suspend fun createDto(withEntity : ENTITY): CommonDTO<DTO, DATA, ENTITY>{
        return this.createDto(null, withEntity)
    }
}