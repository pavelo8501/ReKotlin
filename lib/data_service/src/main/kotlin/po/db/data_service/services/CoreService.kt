package po.db.data_service.services

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.DatabaseManager
import po.db.data_service.annotations.BindProperty
import po.db.data_service.services.models.ContainerModel
import po.db.data_service.services.models.CoreDbEntity
import po.db.data_service.services.models.CoreDbEntityContext
import po.db.data_service.services.models.PropertyMapping
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties



class BaseService (val serviceName: String, val dbManager: DatabaseManager) {


    fun <T : ContainerModel<E>, E>initContainerModel(containerModel: T):T{

        containerModel.setService(this)

        return containerModel
    }


    fun <E>create(body:  () -> E): E{
        val result =  dbQuery{
            body()
        }
        return result
    }

    fun  <T>dbQuery(body : () -> T): T = transaction(dbManager.getConnection()) {
        body()
    }

}


abstract class CoreService <T: ContainerModel<E>, E: CoreDbEntity>(val serviceName: String) {

    abstract val dbManager: DatabaseManager

//    fun getPropertyValues(): Map<String, Any?> {
//        val mappings = getPropertyMappings(this::class)
//        return mappings.associate { mapping ->
//            val value = mapping.property.get()
//            mapping.dbName to value
//        }
//    }

    val serviceScope = CoroutineScope(Job() + Dispatchers.Default + CoroutineName("$serviceName Service  Coroutine"))


//    private suspend fun create(containerModel: T): CoreDbEntity{
//       return dbQueryAsync{
//           val newEntity = containerModel.dbEntityClazz .new {
//                //initContainerModel(containerModel)
//            }
//            newEntity
//        }
//    }

//    fun initContainerModel(containerModel: T):T{
//        //containerModel.load =
//        if(containerModel.id == 0L){
//            serviceScope.launch {
//                create(containerModel)
//            }
//        }
//        return containerModel
//    }





    suspend fun <T> dbQueryAsync(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) {
        block()
    }

}