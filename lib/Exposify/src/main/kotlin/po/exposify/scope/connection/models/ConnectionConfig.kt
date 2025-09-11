package po.exposify.scope.connection.models

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import po.exposify.scope.connection.ConnectionClass


data class ConnectionConfig(
    val hikariConfig: HikariConfig
){

    private var host: String = ""
    private var port: String  = ""
    private var dbName: String  = ""
    private var user: String  = ""
    private var pwd: String  = ""

    constructor(
        host: String,
        port: String,
        dbName: String,
        user: String,
        pwd: String,
        hikariConfig: HikariConfig
    ) : this(hikariConfig){
        this.host = host
        this.port = port
        this.dbName = dbName
        this.user = user
        this.pwd = pwd
    }

    val hikariDataSource: HikariDataSource = HikariDataSource(hikariConfig)

    private val errorList: MutableList<String> = mutableListOf<String>()

    var lastError : String? = null
    var connection: Database? = null
    val driverClassName = "org.postgresql.Driver"


    val key: String get(){
        return "${dbName}@${host}"
    }

    fun registerError(th: Throwable){
        errorList.add(th.message.toString())
    }

    fun getConnectionString(): String{
        if(hikariDataSource.jdbcUrl != null){
            return  hikariDataSource.jdbcUrl
        }else{
            return "jdbc:postgresql://$host:$port/$dbName"
        }
    }
}