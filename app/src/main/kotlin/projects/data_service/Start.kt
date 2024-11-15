package po.playground.projects.data_service

import po.db.data_service.DatabaseManager
import po.db.data_service.models.ConnectionInfo
import po.db.data_service.transportation.ServiceCreateOptions
import po.db.data_service.transportation.TableCreateMode
import po.playground.projects.data_service.dto.DepartmentDTO
import po.playground.projects.data_service.dto.DepartmentEntity
import po.playground.projects.data_service.dto.PartnerDTO
import po.playground.projects.data_service.dto.PartnerEntity

fun startDataService(connectionInfo : ConnectionInfo) {


   DatabaseManager.openConnection(connectionInfo){

      initializeService<PartnerDTO, PartnerEntity>("Partner", PartnerDTO, ServiceCreateOptions(TableCreateMode.FORCE_RECREATE)){
          val partner = PartnerDTO (0, "Partner 1", "Partner 1 SIA", "123456789", "LV123456789", PartnerDTO.nowDateTime(),PartnerDTO.nowDateTime())
          val department = DepartmentDTO(0, true, "Department 1", "Street 1", "Riga", "Latvia", "LV-1010", "27177723", "some@mail.com",12, null,
              DepartmentDTO.nowDateTime(), DepartmentDTO.nowDateTime())
          //partner.
          saveDTO(partner)
      }

      initializeService<DepartmentDTO, DepartmentEntity>("Department", DepartmentDTO, ServiceCreateOptions(TableCreateMode.FORCE_RECREATE)){
         // saveDTO(department)
      }

   }


   val stop = 10

    val partnerJson = """{"id" : 0, "name":"Partner 1", "legal_name" : "Partner 1 SIA"""

   // EntityDTO.CommonBinderClass.
   // val jso = Json.encodeToString(partner)

  //  val partnerDto = PartnerDTO(0, "Partner 1", "Partner 1 SIA", "123456789", "LV123456789", EntityDTO.Companion.nowDateTime(), EntityDTO.Companion.nowDateTime())


  val a = 10




   // partner.departments = departments






    //dbManager.baseService.initContainerModel(department)
   // partner.save()
    //partner.create()
   // partner.update()
    //partner.createTable()
    //partner.load()
  //  partner.update()


  //  val department1 = DepartmentModel(0, 0, true, "Dep1",36)
   // val department2 = DepartmentModel(0, 0, false, "Dep2",12)
   //partner.departments.add(department1)
   // partner.departments.add(department2)

   // val result = dbManager.partnerService().saveModel(partner)


}