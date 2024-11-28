package po.db.data_service.transportation

import io.ktor.util.reflect.TypeInfo
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import po.db.data_service.dto.ModelDTOContext
import po.db.data_service.structure.ServiceContext


enum  class TableCreateMode{
    CREATE,
    FORCE_RECREATE
}

data class ServiceCreateOptions<T : ModelDTOContext ,E : LongEntity>(
    val createTable: TableCreateMode = TableCreateMode.CREATE,
){
    var service: ServiceContext<T,E> ? = null
}


class ServiceRouter(
    private val connectionName: String,
    private val connection: Database,
)
{

    val services :  MutableMap<String, ServiceContext<*,*>> = mutableMapOf()

//    private fun <T : ModelDTOContext, E: LongEntity>getOrCreateService(routeName:String, entityDTOClass: DTOClass<T,E>):ServiceContext<T, E>{
//        services[routeName]?.let {
//            @Suppress("UNCHECKED_CAST")
//            return it as ServiceContext<T,E>
//        }
//        ServiceContext(routeName, connection, entityDTOClass).let {
//            services[routeName] = it
//            return it
//        }
//    }

//    fun <T : ModelDTOContext, E: LongEntity >initializeRoute(
//        routeName: String,
//        entityDTOClass: DTOClass<T,E>,
//        createOptions: ServiceCreateOptions<T,E>? = null
//    ): ServiceContext<T,E> {
//
//        getOrCreateService(routeName, entityDTOClass).let {
//
//            return it
//        }
//    }
}


