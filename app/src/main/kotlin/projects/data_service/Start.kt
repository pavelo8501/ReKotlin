package po.playground.projects.data_service

import po.db.data_service.DatabaseManager
import po.db.data_service.controls.ConnectionInfo
import po.db.data_service.dto.CommonDTO
import po.db.data_service.launchService
import po.db.data_service.scope.service.TableCreateMode
import po.playground.projects.data_service.data_source.asDataModelDynamically
import po.playground.projects.data_service.data_source.asDataModelToDelete
import po.playground.projects.data_service.dto.PartnerDTO
import po.playground.projects.data_service.dto.PartnerDataModel
import po.playground.projects.data_service.dto.PartnerEntity

fun startDataService(connectionInfo : ConnectionInfo) {

    val selected =  mutableListOf<CommonDTO<PartnerDataModel, PartnerEntity>>()

    var toDelete : CommonDTO<PartnerDataModel, PartnerEntity>? = null
    var toModify : CommonDTO<PartnerDataModel, PartnerEntity>? = null

    fun reportResult(result: List<PartnerDataModel>){
        println(result)
    }

    val dbManager =  DatabaseManager
    val connection = dbManager.openConnection(connectionInfo){
        service<PartnerDataModel, PartnerEntity>(PartnerDTO, TableCreateMode.CREATE){

            PartnerDTO.sequence("update_page"){
                select{dtos->
                    dtos.forEach { it.getInjectedModel().name = "New Name" }
                    update(dtos) {  }
                }
            }
        }
    }

    PartnerDTO.triggerSequence("update_page")


   if(connection){
       println("Connection OK")

       println("🔄 Processing... Press Enter to exit.")
       readLine()


   }else{
       throw Exception("Connection not established")
   }

}