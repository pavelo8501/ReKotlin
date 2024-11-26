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

      initializeService<PartnerDTO, PartnerEntity>("Partner", PartnerDTO, ServiceCreateOptions(TableCreateMode.CREATE)){
          val department = DepartmentDTO(0, true, "Department 1", "Street 1", "Riga", "Latvia", "LV-1010", "27177723", "some@mail.com",12, null,
              DepartmentDTO.nowDateTime(), DepartmentDTO.nowDateTime())
          val partner = PartnerDTO (0, "Partner 1", "Partner 1 SIA", "123456789", "LV123456789", PartnerDTO.nowDateTime(),PartnerDTO.nowDateTime())
          partner.departments.add(department)
          saveDTO(partner)
      }

   }

   val stop = 10
}