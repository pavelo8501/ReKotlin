package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.constructors.ClassBlueprintContainer
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.dto.DTOClass
import po.db.data_service.exceptions.InitializationException
import kotlin.reflect.KClass

class ServiceClass<ENTITY>(
    private val connection :Database,
    private val rootDTOModel : DTOClass<ENTITY>,
)  where  ENTITY : LongEntity{

   companion object :  ConstructorBuilder()

   var name : String = "undefined"

    init {
        try {
            start()
        }catch (initException : InitializationException){
            println(initException.message)
        }
    }

    private fun getClassBlueprint(dtoModel: DTOClass<*>):ClassBlueprintContainer{
        dtoModel.conf.also {
           return ClassBlueprintContainer(
                getConstructorBlueprint<Any>(it.dtoModelClass as KClass<*>),
                getConstructorBlueprint<Any>(it.dataModelClass as KClass<*>)
            )
        }
    }

    private fun initializeDTOs(context: ServiceClass<ENTITY>.() -> Unit ) {
        context.invoke(this)
    }

    private fun start(){
        initializeDTOs{
            rootDTOModel.initialization{
                getClassBlueprint(it)
            }
            name = " ${rootDTOModel.className}|Service"
        }
    }

    fun launch(receiver: ServiceContextV2<ENTITY>.()->Unit ){
        val serviceContext = ServiceContextV2(connection, rootDTOModel)
        serviceContext.receiver()
    }


//        serviceRegistry.addServiceRegistryItem {
//            key = ServiceUniqueKey("TestRun")
//            metadata {
//                key = ServiceUniqueKey("TestRun")
//                service {
//                    rootDTOModel.initialization()
//                }
//            }
//        }

//        rootDataModel.initialization().also { dtoClass->
//           dtoClass.dtoContext.also {
//              val dtoData =  DTOData(it.dtoModelClass, it.entityModel, it.dataModelClass)
//               serviceRegistry.addServiceRegistryItem {
//                   key = ServiceUniqueKey(dtoClass.dtoContext.name)
//                   metadata {
//                       key = ServiceUniqueKey(dtoClass.dtoContext.name)
//                       service {
//                           rootDTOModelData = dtoData
//                       }
//                   }
//               }
//            }
//        }
       // val vb  = 10


  //  val serviceRouter = ServiceRouter(connectionName, connection, serviceRegistry)
}