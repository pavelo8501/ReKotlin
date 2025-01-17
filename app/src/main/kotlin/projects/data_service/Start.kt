package po.playground.projects.data_service

import po.db.data_service.DatabaseManager
import po.db.data_service.controls.ConnectionInfo
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.TableCreateMode
import po.db.data_service.scope.service.enums.WriteMode
import po.playground.projects.data_service.data_source.asDataModelDynamically
import po.playground.projects.data_service.dto.PartnerDTO
import po.playground.projects.data_service.dto.PartnerDataModel
import po.playground.projects.data_service.dto.PartnerEntity

fun startDataService(connectionInfo : ConnectionInfo) {

    val selected =  mutableListOf<CommonDTO<PartnerDataModel, PartnerEntity>>()

    var toDelete : CommonDTO<PartnerDataModel, PartnerEntity>? = null

    val dbManager =  DatabaseManager
    val connection = dbManager.openConnection(connectionInfo){
        service<PartnerDataModel, PartnerEntity>(PartnerDTO, TableCreateMode.CREATE){

            PartnerDTO.select{
                toDelete = result()[0]
            }

//            if(toDelete!= null){
//                PartnerDTO.delete(toDelete.injectedDataModel){
//
//                }
//            }

//            PartnerDTO.update(asDataModelDynamically(partnerCount = 4, departmentCount = 5), WriteMode.RELAXED){
//                getStats()
//            }

        }
    }

   println(selected)

   if(connection){
       println("Connection OK")
   }else{
       throw Exception("Connection not established")
   }

}