package po.playground.projects.data_service

import po.db.data_service.DatabaseManager
import po.db.data_service.controls.ConnectionInfo
import po.db.data_service.scope.service.TableCreateMode
import po.playground.projects.data_service.data_source.asDTO
import po.playground.projects.data_service.data_source.asDataModels
import po.playground.projects.data_service.dto.*


fun startDataService(connectionInfo : ConnectionInfo) {

    val dbManager =  DatabaseManager

    val connection = dbManager.openConnection(connectionInfo){

        service<PartnerDataModel, PartnerEntity>(PartnerDTO, TableCreateMode.CREATE){

            PartnerDTO.update(asDataModels()){
            }
            PartnerDTO.select {
            }
        }
    }
   if(connection){
       println("Connection OK")
   }else{
       throw Exception("Connection not established")
   }

}