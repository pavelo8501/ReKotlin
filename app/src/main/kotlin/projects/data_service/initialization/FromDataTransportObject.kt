package po.playground.projects.data_service.initialization

import po.playground.projects.data_service.dto.DepartmentDTO
import po.playground.projects.data_service.dto.DepartmentDataModel
import po.playground.projects.data_service.dto.PartnerDTO
import po.playground.projects.data_service.dto.PartnerDataModel


val dataModel = PartnerDataModel("Partner 1", "Partner 1 SIA", null, null).also {
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
}

fun initFromDTO():PartnerDTO{
   return PartnerDTO(dataModel)
}
