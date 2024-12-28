package po.playground

import io.github.cdimascio.dotenv.dotenv
import po.db.data_service.DatabaseManager
import po.db.data_service.controls.ConnectionInfo
import po.db.data_service.scope.service.TableCreateMode
import po.playground.projects.data_service.dto.PartnerDTO
import po.playground.projects.data_service.dto.PartnerEntity
import po.playground.projects.data_service.initialization.initFromDTO
import po.playground.projects.data_service.initialization.initFromDataModel

fun main() {

   val partnerDto = initFromDTO()


   val ba = "stop"

    val dotenv = dotenv()
    val driver = dotenv["MYSQL_DRIVER"]
    val dbHost = dotenv["MYSQL_HOST"]
    val dbPort = dotenv["MYSQL_PORT"]
    val dbName = dotenv["MYSQL_DATABASE"]
    val dbUsername = dotenv["MYSQL_USER"]
    val dbPassword = dotenv["MYSQL_PASSWORD"]

 val dbManager =  DatabaseManager

 val connection = dbManager.openConnection(ConnectionInfo(dbHost, dbName, dbUsername, dbPassword, dbPort)){
     service<PartnerDTO,PartnerEntity>(PartnerDTO, TableCreateMode.FORCE_RECREATE){
         PartnerDTO.update(initFromDataModel()){

         }
     }
 }




    //startDataService(ConnectionInfo(dbHost, dbName, dbUsername, dbPassword, dbPort))






 //   startApiServer(host, port)


   // startWebSocketServer(host, port)

   // testConverter()

    val a = 10

}