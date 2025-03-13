package po.playground.projects.data_service

import kotlinx.coroutines.runBlocking
import po.exposify.DatabaseManager
import po.exposify.controls.ConnectionInfo

import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.service.TableCreateMode
import po.playground.projects.data_service.data_source.asDataModels
import po.playground.projects.data_service.dto.PartnerDTO
import po.playground.projects.data_service.dto.PartnerDataModel
import po.playground.projects.data_service.dto.PartnerEntity

object PartnerUpdate : SequenceHandler<PartnerDataModel>(PartnerDTO, "update_partner")



fun mockOfRestRequest(){

    runBlocking {
        val partner = PartnerDataModel("SomeName", "SomeName SIA")
        PartnerUpdate.execute(asDataModels()) {
            println(it)
        }
    }
}


suspend fun startDataService(connectionInfo : ConnectionInfo) {

    val dbManager = DatabaseManager

    val connection = dbManager.openConnection(connectionInfo) {
        service<PartnerDataModel, PartnerEntity>(PartnerDTO, TableCreateMode.FORCE_RECREATE) {
            PartnerDTO.sequence(PartnerUpdate) {data->
                update(data) { checkout() }
            }

//            PageDTO.sequence(PageUpdate){inputData->
//                update(inputData){ checkout() }
//            }
        }
    }

    mockOfRestRequest()
    if (connection) {
        println("Connection OK")

        println("🔄 Processing... Press Enter to exit.")
        readLine()

    }else{
        throw Exception("Connection not established")
    }
}
