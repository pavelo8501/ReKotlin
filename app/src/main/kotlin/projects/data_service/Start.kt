package po.playground.projects.data_service

import po.exposify.DatabaseManager
import po.exposify.controls.ConnectionInfo
import po.exposify.dto.CommonDTO
import po.exposify.launchService
import po.exposify.scope.service.TableCreateMode
import po.playground.projects.data_service.data_source.asDataModelDynamically
import po.playground.projects.data_service.data_source.asDataModelToDelete
import po.playground.projects.data_service.dto.DepartmentDTO
import po.playground.projects.data_service.dto.DepartmentDataModel
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

    fun returnData(data : List<DepartmentDataModel>){
        println(data)
    }

    val dbManager =  DatabaseManager
    val connection = dbManager.openConnection(connectionInfo){
        service<PartnerDataModel, PartnerEntity>(PartnerDTO, TableCreateMode.CREATE){
            PartnerDTO.sequence("update_page"){
                select{
                    DepartmentDTO.switch{
                        it.checkout{
                        callbackOnResult {
                            returnData(it)
                        } }
                    }
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