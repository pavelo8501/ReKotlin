package po.db.data_service

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import po.db.data_service.exceptions.DataServiceException
import po.db.data_service.exceptions.ErrorCodes
import po.db.data_service.models.ConnectionModel
import po.db.data_service.services.DataServiceDataContext


abstract class DatabaseManager(val connectionInfo: ConnectionModel) {

    val services =  mutableMapOf<String, DataServiceDataContext<*>>()
    private var connection : Database? = null

    init {
        val dataSource = provideDataSource(connectionInfo.getConnectionString())
        connection = Database.connect(dataSource)
        init()
    }

    private fun provideDataSource(url:String): HikariDataSource {
        val hikariConfig= HikariConfig().apply {
            driverClassName=connectionInfo.driverClassName
            jdbcUrl=connectionInfo.getConnectionString()
            maximumPoolSize=10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(hikariConfig)
    }

    fun addService(name:String, service : DataServiceDataContext<*>){
        this.services[name] = service
    }
    fun getService(name:String): DataServiceDataContext<*>{
        if(services.containsKey(name) == false){
            throw DataServiceException("Service $name not found", ErrorCodes.NOT_INITIALIZED)
        }
        return services[name]!!
    }

    abstract fun init()

    fun getConnection(): Database{
        if(connection == null){
            throw Exception("Connection not initialized")
        }
        return connection!!
    }

}