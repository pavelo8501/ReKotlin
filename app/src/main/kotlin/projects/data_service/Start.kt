package po.playground.projects.data_service

import po.db.data_service.DatabaseManager
import po.db.data_service.controls.ConnectionInfo
import po.db.data_service.scope.service.TableCreateMode
import po.playground.projects.data_service.dto.*


fun startDataService(connectionInfo : ConnectionInfo) {

    val partnerList = listOf<PartnerDataModel>(
        PartnerDataModel(
            0,
            "Partner 1",
            "Partner 1 SIA",
            null,
            null,
            PartnerDTO.nowTime(),
            PartnerDTO.nowTime()).also {
            it.departments.addAll(
               listOf(DepartmentDataModel(
                    0L,
                    true,
                    "Partner 1 Department 1",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    12,
                    null,
                   DepartmentDTO.nowTime(),
                   DepartmentDTO.nowTime()
                ),
                   DepartmentDataModel(
                       0L,
                       false,
                       "Partner 1 Department 2",
                       null,
                       null,
                       null,
                       null,
                       null,
                       null,
                       12,
                       null,
                       DepartmentDTO.nowTime(),
                       DepartmentDTO.nowTime()
                   )
                )
            )
        },
        PartnerDataModel(0,"Partner 2", "Partner 2 SIA", null,null,  PartnerDTO.nowTime(), PartnerDTO.nowTime()).also {
            it.departments.addAll(
                listOf(
                    DepartmentDataModel(
                    0L,
                    true,
                    "Partner 2 Department 1",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    12,
                    null,
                    DepartmentDTO.nowTime(),
                    DepartmentDTO.nowTime()
                ),
                    DepartmentDataModel(
                        0L,
                        false,
                        "Partner 2 Department 2",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        12,
                        null,
                        DepartmentDTO.nowTime(),
                        DepartmentDTO.nowTime()
                    ),
                    DepartmentDataModel(
                        0L,
                        false,
                        "Partner 2 Department 3",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        12,
                        null,
                        DepartmentDTO.nowTime(),
                        DepartmentDTO.nowTime()
                    )
                )
            )
        },
        PartnerDataModel(0,"Partner 3", "Partner 3 SIA", null,null,  PartnerDTO.nowTime(), PartnerDTO.nowTime()).also {
            it.departments.add(
                DepartmentDataModel(
                    0L,
                    true,
                    "Partner 3 Department 1",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    12,
                    null,
                    DepartmentDTO.nowTime(),
                    DepartmentDTO.nowTime()
                ),
            )
        }
    )

    DatabaseManager.openConnection(connectionInfo){
        service<PartnerDTO, PartnerEntity>(PartnerDTO, TableCreateMode.CREATE){

            PartnerDTO.select {

            }
        }
   }
}