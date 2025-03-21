package po.playground.projects.data_service

import kotlinx.coroutines.runBlocking
import po.exposify.DatabaseManager
import po.exposify.controls.ConnectionInfo


import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll


import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.service.TableCreateMode
import po.playground.projects.data_service.data_source.asDataModels
import po.playground.projects.data_service.dto.DepartmentDTO
import po.playground.projects.data_service.dto.DepartmentDataModel
import po.playground.projects.data_service.dto.DepartmentEntity
import po.playground.projects.data_service.dto.PartnerDTO
import po.playground.projects.data_service.dto.PartnerDataModel
import po.playground.projects.data_service.dto.PartnerEntity
import po.playground.projects.data_service.services.Departments
import po.playground.projects.data_service.services.Partners

object PartnerUpdate : SequenceHandler<PartnerDataModel>(PartnerDTO, "update_partner")
object PartnerSelect : SequenceHandler<PartnerDataModel>(PartnerDTO, "select_partners")
object PartnerPick : SequenceHandler<PartnerDataModel>(PartnerDTO, "pick_partner")

object DepartmentSelect: SequenceHandler<DepartmentDataModel>(DepartmentDTO, "select_departments")

fun mockOfRestUpdateRequest(){
    runBlocking {
        val partner = PartnerDataModel("SomeName", "SomeName SIA")
        val result =  PartnerUpdate.execute(asDataModels()).await()
    }
}

fun mockOfRestGetRequest(){
    runBlocking {
        val frequency = 36
        val result =   PartnerSelect.execute(Partners.name eq  "Partner 2").await()
        println(result)
    }
}

fun mockOfRestGetSingleRequest(){
    runBlocking {
        val result =  PartnerPick.execute(asDataModels())
        println(result)
    }
}


suspend fun startDataService(connectionInfo : ConnectionInfo) {

    val dbManager = DatabaseManager

    val connection = dbManager.openConnection(connectionInfo) {
        service<PartnerDataModel, PartnerEntity>(PartnerDTO, TableCreateMode.CREATE) {

            select<Partners, Departments>(Partners, Departments) {
                (Departments.name eq  "Partner 2 Department 1") and  (Partners.legalName like "%SIA%")
            }

//            sequence(PartnerUpdate) {conditions, data ->
//                update(data) { checkout() }
//            }
//
//            sequence(PartnerSelect) {conditions, data ->
//                select(conditions) { checkout() }
//            }

        }

        service<DepartmentDataModel, DepartmentEntity>(DepartmentDTO, TableCreateMode.CREATE) {

        }
    }

   // mockOfRestGetRequest()
    if (connection) {
        println("Connection OK")

        println("🔄 Processing... Press Enter to exit.")
        readLine()

    }else{
        throw Exception("Connection not established")
    }
}
