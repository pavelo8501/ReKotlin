package po.playground.projects.data_service

import kotlinx.coroutines.awaitAll
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
object PartnerPick : SequenceHandler<PartnerDataModel>(PartnerDTO, "pick_partner")


fun mockOfRestUpdateRequest(){
    runBlocking {
        val partner = PartnerDataModel("SomeName", "SomeName SIA")
        PartnerUpdate.execute(asDataModels()) {
            println(it)
        }
    }
}

fun mockOfRestGetRequest(){
    runBlocking {
        val name = "Partner 2"
        val result =   PartnerSelect.execute(PartnerDataModel::name to  name).await()
        println(result)
    }
}

fun mockOfRestGetSingleRequest(){
    runBlocking {
        PartnerPick.execute(asDataModels()) {
            println(it)
        }
    }
}

suspend fun startDataService(connectionInfo : ConnectionInfo) {

    val dbManager = DatabaseManager

    val connection = dbManager.openConnection(connectionInfo) {
        service<PartnerDataModel, PartnerEntity>(PartnerDTO, TableCreateMode.CREATE) {
            PartnerDTO.sequence(PartnerUpdate) {conditions, data ->
                update(data) { checkout() }
            }

            PartnerDTO.sequence (PartnerSelect) {conditions, data ->
                select(conditions) { checkout() }
            }

            PartnerDTO.sequence (PartnerSelect) {conditions, data ->
                select() { checkout() }
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
