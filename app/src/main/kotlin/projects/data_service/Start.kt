package po.playground.projects.data_service

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.DatabaseManager
import po.db.data_service.dao.EntityDAO
import po.db.data_service.dto.AbstractDTOModel
import po.db.data_service.models.ConnectionInfo
import po.db.data_service.transportation.ServiceCreateOptions
import po.playground.projects.data_service.dto.Partner
import po.playground.projects.data_service.dto.PartnerEntity


fun startDataService(connectionInfo : ConnectionInfo) {



//   DatabaseManager.openConnection(connectionInfo){
//      runTest<Partner>(Partner(0,"SomeName", "SomeName SIA", null,null, Partner.nowTime(),Partner.nowTime()))
//   }

  DatabaseManager.openConnection(connectionInfo){
   //   val model = EntityDAO<Partner, PartnerEntity>::entityDao



    initializeService<Partner, PartnerEntity>("Partner", Partner, PartnerEntity){

        val partner =  Partner(0,"SomeName", "SomeName SIA", null,null, Partner.nowTime(),Partner.nowTime())

        saveDtoEntity(partner)

     //     val department = DepartmentDTO(0, true, "Department 1", "Street 1", "Riga", "Latvia", "LV-1010", "27177723", "some@mail.com",12, null,
//             // DepartmentDTO.nowDateTime(), DepartmentDTO.nowDateTime())
//          val partner = PartnerDTO (0, "Partner 1", "Partner 1 SIA", "123456789", "LV123456789", PartnerDTO.nowDateTime(),PartnerDTO.nowDateTime())
//         // partner.departments.add(department)
//          saveDTO(partner)
      }
   }

   val stop = 10
}