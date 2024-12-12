package po.playground.projects.data_service

import po.db.data_service.DatabaseManager
import po.db.data_service.models.ConnectionInfo
import po.playground.projects.data_service.dto.Partner
import po.playground.projects.data_service.dto.PartnerEntity


fun startDataService(connectionInfo : ConnectionInfo) {

    val list = listOf<Partner>(
        Partner(0,"SomeName", "SomeName SIA", null,null,    Partner.nowTime(),Partner.nowTime()),
        Partner(0,"SomeName2", "SomeName2 SIA", null,null,  Partner.nowTime(),Partner.nowTime()),
        Partner(0,"SomeName3", "SomeName2 SIA", null,null,  Partner.nowTime(),Partner.nowTime())
    )

    DatabaseManager.openConnection(connectionInfo){
        service<Partner, PartnerEntity>("Partner",  Partner, PartnerEntity){
//            Partner.update(list) {
//            }

            Partner.select {
                val list = it
                val a = 10
            }

     //     val department = DepartmentDTO(0, true, "Department 1", "Street 1", "Riga", "Latvia", "LV-1010", "27177723", "some@mail.com",12, null,
//             // DepartmentDTO.nowDateTime(), DepartmentDTO.nowDateTime())
//          val partner = PartnerDTO (0, "Partner 1", "Partner 1 SIA", "123456789", "LV123456789", PartnerDTO.nowDateTime(),PartnerDTO.nowDateTime())
//         // partner.departments.add(department)
//          saveDTO(partner)
      }
   }
   val dataModel =  list[0].asDataModel()
   val stop = 10
}