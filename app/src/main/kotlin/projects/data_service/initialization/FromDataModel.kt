package po.playground.projects.data_service.initialization

import po.playground.projects.data_service.dto.DepartmentDTO
import po.playground.projects.data_service.dto.DepartmentDataModel
import po.playground.projects.data_service.dto.PartnerDataModel

fun initFromDataModel():List<PartnerDataModel>{

return listOf<PartnerDataModel>(
    PartnerDataModel(
        "Partner 1",
        "Partner 1 SIA",
        null,
        null).also {
        it.departments.addAll(
            listOf(
                DepartmentDataModel(
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
    PartnerDataModel("Partner 2", "Partner 2 SIA", null,null).also {
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
    PartnerDataModel("Partner 3", "Partner 3 SIA", null,null).also {
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
}