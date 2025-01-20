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
    var toModify : CommonDTO<PartnerDataModel, PartnerEntity>? = null

    val dbManager =  DatabaseManager
    val connection = dbManager.openConnection(connectionInfo){
        service<PartnerDataModel, PartnerEntity>(PartnerDTO, TableCreateMode.FORCE_RECREATE){

//            PartnerDTO.select{
//                toModify = result()[1]
//            }

//            if(toModify!= null){
//                val dataModel = toModify.getDataModel()
//                dataModel.name = "Updated"
//                PartnerDTO.update(listOf(dataModel)){
//
//                }
//            }

//            if(toDelete!= null){
//                PartnerDTO.delete(toDelete.injectedDataModel){
//
//                }
//            }

            PartnerDTO.update(asDataModelDynamically(partnerCount = 1, departmentCount = 1, false)){
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