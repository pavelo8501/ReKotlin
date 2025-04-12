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
import po.lognotify.extensions.getOrThrowDefault
import po.lognotify.extensions.subTask
import kotlin.collections.get
import kotlin.reflect.KClass
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

    private var serializers = mapOf<String, KSerializer<out Any>>()

    fun setSerializableTypes(types: List<Pair<String, KSerializer<out Any>>>){
        serializers =  types.associate {
            it.first to  it.second
        }
    }

    fun setDataModelConstructor(dataModelConstructor : (() -> DATA)){
        this.dataModelConstructor = dataModelConstructor
    }

    internal var postCreationRoutines = MapBuilder<String, suspend CommonDTO<DTO, DATA, ENTITY>.() -> Unit>()
    fun setPostCreationRoutine(name : String, fn: suspend CommonDTO<DTO, DATA, ENTITY>.()-> Unit){
        postCreationRoutines.put(name, fn)
    }
    fun unsetPostCreationRoutine(){
        postCreationRoutines.clear()
    }

    suspend fun dtoPostCreation(dto : CommonDTO<DTO, DATA, ENTITY>){
        postCreationRoutines.map.forEach {
            it.value.invoke(dto)
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
                val foundSerializer =  serializers[param.name].getOrThrowDefault("Serializer for name: ${param.name} not found")

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

    /**
     * Create new instance of  DTOFunctions
     * if input param dataModel provided use it as an injection into constructor
     * if not then create new DataModel instance with default parameters i.e. no data will be preserved
     * @input dataModel:  DATA?
     * @return DTOFunctions<DATA, ENTITY> or null
     * */
    suspend fun createDto(withDataModel : DATA? = null): CommonDTO<DTO, DATA, ENTITY> =
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
        dtoPostCreation(newDto)
        newDto
    }.resultOrException()

}