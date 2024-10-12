package po.db.data_service.models

class ConnectionModel(val host: String, val dbName: String, val user: String, val pwd: String, val port: String = "3306") {

    val driverClassName = "com.mysql.cj.jdbc.Driver"

    fun getConnectionString(): String{
        return  "jdbc:mysql://$host:$port/$dbName?user=$user&password=$pwd"
    }
}