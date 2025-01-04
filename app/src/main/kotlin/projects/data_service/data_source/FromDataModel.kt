package po.playground.projects.data_service.data_source

import po.playground.projects.data_service.dto.DepartmentDataModel
import po.playground.projects.data_service.dto.PartnerDTO
import po.playground.projects.data_service.dto.PartnerDataModel

fun asDataModels():List<PartnerDataModel>{

    return listOf<PartnerDataModel>(
        PartnerDataModel("Partner 1", "Partner 1 SIA").also {
            it.departments.addAll(
                listOf(
                    DepartmentDataModel(true, "Partner 1 Department 1", 12),
                    DepartmentDataModel(false, "Partner 1 Department 2", 24)
                )
            )
        },
        PartnerDataModel("Partner 2", "Partner 2 SIA").also {
            it.departments.addAll(
                listOf(
                    DepartmentDataModel(true, "Partner 2 Department 1", 12),
                    DepartmentDataModel(false, "Partner 2 Department 2", 36),
                    DepartmentDataModel(false, "Partner 2 Department 3", 24)
                )
            )
        },
        PartnerDataModel("Partner 3", "Partner 3 SIA").also {
            it.departments.add(
                DepartmentDataModel(true, "Partner 3 Department 1", 12),
            )
        }
    )
}

fun asDTO(): List<PartnerDTO> {
    return asDataModels().map { PartnerDTO(it) }
}