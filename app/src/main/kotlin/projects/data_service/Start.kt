package po.playground.projects.data_service

import po.db.data_service.DatabaseManager
import po.db.data_service.controls.ConnectionInfo
import po.db.data_service.dto.CommonDTO
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

            PartnerDTO.select{
                resultAsDataModel{
                    reportResult(it)
                }
            }

//            PartnerDTO.delete(asDataModelToDelete()){
//
//            }


//            if(toDelete!= null){
//                PartnerDTO.delete(toDelete.injectedDataModel){
//
//                }
//            }

//            PartnerDTO.update(asDataModelDynamically(partnerCount = 50, departmentCount = 20, true)){
//                resultAsDataModel{
//                    println(it)
//                }
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