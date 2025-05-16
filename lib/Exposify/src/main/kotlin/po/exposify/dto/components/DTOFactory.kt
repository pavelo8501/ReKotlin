package po.exposify.dto.components

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.common.classes.ClassBlueprint
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.addTrackerInfo
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import kotlin.collections.get
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


internal class PostCreationRoutine<DTO, DATA, ENTITY, R>(
    val name: String,
   private val routineBlock: suspend CommonDTO<DTO, DATA, ENTITY>.()->R,
) where DTO : ModelDTO, DATA: DataModel, ENTITY: LongEntity{

   val resultDeferred: CompletableDeferred<R> = CompletableDeferred()

   suspend fun invokeRoutineBlock(receiver: CommonDTO<DTO, DATA, ENTITY>){
      resultDeferred.complete(routineBlock.invoke(receiver))
    }
}

class DTOFactory<DTO, DATA, ENTITY>(
    private val dtoKClass : KClass<DTO>,
    private val dataModelClass : KClass<DATA>,
    private val hostingConfig: DTOConfig<DTO, DATA, ENTITY>,
): IdentifiableComponent,  TasksManaged where DTO : ModelDTO, DATA: DataModel, ENTITY: LongEntity {


    override val qualifiedName: String = "DTOFactory[${hostingConfig.registryRecord.entityName}]"
    override val type: ComponentType = ComponentType.Factory

    private val personalName = "DTOFactory[${dtoKClass.simpleName}]"

    internal val dataBlueprint : ClassBlueprint<DATA> =  ClassBlueprint(dataModelClass)
    private val dtoBlueprint : ClassBlueprint<DTO> = ClassBlueprint(dtoKClass)

    private var dataModelBuilderFn : (() -> DATA)? = null

    private var serializers = mutableMapOf<String, KSerializer<*>>()


    fun <T> setSerializableType(name: String, serializer : KSerializer<T>){
        serializers[name] = serializer
    }

    fun setDataModelConstructor(dataModelBuilder : (() -> DATA)){
        dataModelBuilderFn = dataModelBuilder
    }

    internal var postCreationRoutines : MutableMap<String, PostCreationRoutine<DTO, DATA, ENTITY, *>> = mutableMapOf()
    suspend fun <R> setPostCreationRoutine(
        name : String,
        block: suspend CommonDTO<DTO, DATA, ENTITY>.()-> R
    ): Deferred<R>{
        val routine = PostCreationRoutine(name, block)
        postCreationRoutines.put(name,  routine)
        return routine.resultDeferred
    }

    fun unsetPostCreationRoutine(){
        postCreationRoutines.clear()
    }

    private suspend fun dtoPostCreation(dto : CommonDTO<DTO, DATA, ENTITY>)
        = subTask("dtoPostCreation"){handler->
        dto.addTrackerInfo(CrudOperation.Initialize, this)
        dto.initialize()
        postCreationRoutines.values.forEach {
            handler.info("Executing ${it.name}")
            it.invokeRoutineBlock(dto)
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
            dataModelBuilderFn?.let {
                return it.invoke()
            }

            dataBlueprint.setExternalParamLookupFn { param ->
                var paramValue : Any? = null
                val foundSerializer =  serializers[param.name].getOrOperationsEx(
                    "Serializer for name: ${param.name} not found",
                    ExceptionCode.VALUE_NOT_FOUND)

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
            val args = dataBlueprint.getConstructorArgs()
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
        subTask("Create DTO", personalName){handler->
        val dataModel = withDataModel?: createDataModel()
        dtoBlueprint.setExternalParamLookupFn { param ->
            when (param.name) {
                "dataModel" -> {
                    if (param.type.classifier == dataModel::class) {
                        dataModel
                    } else {
                        throw IllegalArgumentException("Mismatched dataModel type for ${dtoBlueprint.clazz.simpleName}. Expected ${param.type}, got ${dataModel::class}")
                    }
                }
                else -> {
                    null
                }
            }
        }
       try {
            val args = dtoBlueprint.getConstructorArgs()
            val newDto =  dtoBlueprint.getConstructor().callBy(args).castOrOperationsEx<CommonDTO<DTO, DATA, ENTITY>>()
            dtoPostCreation(newDto)
            newDto
        }catch (th: Throwable){
           val argsUsed = dtoBlueprint.getConstructorArgs()
           val argsStr =  argsUsed.keys.joinToString { it.name.toString() }
           handler.warn("While creating dto for $argsStr")
           handler.warn("Arguments used ${argsUsed.values.map { toString() }}")
          handler.warn(th.toString().prependIndent("Exception"))
          throw  th
        }
    }.resultOrException()

}