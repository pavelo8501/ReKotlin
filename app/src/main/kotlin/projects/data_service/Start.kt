package po.playground.projects.data_service

import po.db.data_service.DatabaseManager
import po.db.data_service.controls.ConnectionInfo
import po.db.data_service.models.EntityDTO
import po.db.data_service.scope.service.TableCreateMode
import po.db.data_service.scope.service.enums.WriteMode
import po.playground.projects.data_service.data_source.asDataModelDynamically
import po.playground.projects.data_service.dto.PartnerDTO
import po.playground.projects.data_service.dto.PartnerDataModel
import po.playground.projects.data_service.dto.PartnerEntity

fun startDataService(connectionInfo : ConnectionInfo) {

    val selected =  mutableListOf<EntityDTO<PartnerDataModel, PartnerEntity>>()

    val dbManager =  DatabaseManager
    val connection = dbManager.openConnection(connectionInfo){
        service<PartnerDataModel, PartnerEntity>(PartnerDTO, TableCreateMode.FORCE_RECREATE){

            PartnerDTO.update(asDataModelDynamically(partnerCount = 2, departmentCount = 2), WriteMode.RELAXED){
                getStats()
            }

        }
    }

   println(selected)

   if(connection){
       println("Connection OK")
   }else{
       throw Exception("Connection not established")
   }

}