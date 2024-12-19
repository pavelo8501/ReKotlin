package po.playground.projects.data_service

import po.db.data_service.DatabaseManager
import po.db.data_service.controls.ConnectionInfo
import po.playground.projects.data_service.dto.PartnerDTO
import po.playground.projects.data_service.dto.PartnerDTOV2
import po.playground.projects.data_service.dto.PartnerDataModel
import po.playground.projects.data_service.dto.PartnerEntity


fun startDataService(connectionInfo : ConnectionInfo) {

    val list = listOf<PartnerDataModel>(
        PartnerDataModel(0,"SomeName", "SomeName SIA", null,null,    PartnerDTO.nowTime(), PartnerDTO.nowTime()),
        PartnerDataModel(0,"SomeName2", "SomeName2 SIA", null,null,  PartnerDTO.nowTime(), PartnerDTO.nowTime()),
        PartnerDataModel(0,"SomeName3", "SomeName2 SIA", null,null,  PartnerDTO.nowTime(), PartnerDTO.nowTime())
    )

    DatabaseManager.openConnection(connectionInfo){

        serviceV2<PartnerDTOV2>(PartnerDTOV2){

            PartnerDTOV2.select {


            }

        }

      //  service(PartnerDTO){

            //            Partner.update(list) {
            // }

//            PartnerDTO.select {
//                val list = it
//                list[0].copy()
//                val a = 10
//            }

     //     val department = DepartmentDTO(0, true, "Department 1", "Street 1", "Riga", "Latvia", "LV-1010", "27177723", "some@mail.com",12, null,
//             // DepartmentDTO.nowDateTime(), DepartmentDTO.nowDateTime())
//          val partner = PartnerDTO (0, "Partner 1", "Partner 1 SIA", "123456789", "LV123456789", PartnerDTO.nowDateTime(),PartnerDTO.nowDateTime())
//         // partner.departments.add(department)
//          saveDTO(partner)
  //    }
   }
   //val dataModel =  list[0].asDataModel()
 //  val stop = 10
}