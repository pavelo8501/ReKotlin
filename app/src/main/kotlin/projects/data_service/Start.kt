package po.playground.projects.data_service

import kotlinx.coroutines.runBlocking
import po.exposify.DatabaseManager
import po.exposify.controls.ConnectionInfo
import po.exposify.dto.CommonDTO
import po.exposify.launchService
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.service.TableCreateMode
import po.playground.projects.data_service.data_source.asDataModelDynamically
import po.playground.projects.data_service.data_source.asDataModelToDelete
import po.playground.projects.data_service.dto.DepartmentDTO
import po.playground.projects.data_service.dto.DepartmentDataModel
import po.playground.projects.data_service.dto.PartnerDTO
import po.playground.projects.data_service.dto.PartnerDataModel
import po.playground.projects.data_service.dto.PartnerEntity

object PartnerUpdate :
    SequenceHandler<PartnerDataModel>(PartnerDTO, "update_partner")


fun mockOfRestRequest(){

    runBlocking {
        val partner = PartnerDataModel("SomeName", "SomeName SIA")
        PartnerUpdate.execute(listOf<PartnerDataModel>(partner)){
            println(it)
        }
    }
}


fun startDataService(connectionInfo : ConnectionInfo) {

    val dbManager =  DatabaseManager
    val connection = dbManager.openConnection(connectionInfo){
        service<PartnerDataModel, PartnerEntity>(PartnerDTO, TableCreateMode.CREATE){
            PartnerDTO.sequence(PartnerUpdate){
                update{ checkout() }
            }
        }
    }
    mockOfRestRequest()
   if(connection){
       println("Connection OK")

       println("🔄 Processing... Press Enter to exit.")
       readLine()

   }else{
       throw Exception("Connection not established")
   }
}