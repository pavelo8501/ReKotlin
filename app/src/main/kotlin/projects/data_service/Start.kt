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
object PartnerSelect : SequenceHandler<PartnerDataModel>(PartnerDTO, "select_partners")

fun mockOfRestRequest(){
    runBlocking {
        val partner = PartnerDataModel("SomeName", "SomeName SIA")
        PartnerUpdate.execute(asDataModels()) {
            println(it)
        }
    }
}

fun mockOfRestGetRequest(){
    runBlocking {

        PartnerUpdate.execute(asDataModels()) {
            println(it)
        }
    }
}



suspend fun startDataService(connectionInfo : ConnectionInfo) {

    val dbManager = DatabaseManager

    val connection = dbManager.openConnection(connectionInfo) {
        service<PartnerDataModel, PartnerEntity>(PartnerDTO, TableCreateMode.CREATE) {
            PartnerDTO.sequence(PartnerUpdate) {data->
                update(data) { checkout() }
            }

            PartnerDTO.sequence(PartnerSelect) { data->
                select(data) { checkout() }
            }

        }
    }

    mockOfRestGetRequest()
    if (connection) {
        println("Connection OK")

        println("🔄 Processing... Press Enter to exit.")
        readLine()

    }else{
        throw Exception("Connection not established")
    }
}
