package po.playground.projects.data_service.data_source

import po.playground.projects.data_service.dto.ContactDataModel
import po.playground.projects.data_service.dto.DepartmentDataModel
import po.playground.projects.data_service.dto.InspectionDTO
import po.playground.projects.data_service.dto.InspectionDataModel
import po.playground.projects.data_service.dto.PartnerDTO
import po.playground.projects.data_service.dto.PartnerDataModel
import kotlin.random.Random

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

fun asDataModelDynamically(
    partnerCount: Int,
    departmentCount: Int,
    randomizeDepartmentCount: Boolean = true
):List<PartnerDataModel>{
    val result = mutableListOf<PartnerDataModel>()
    for(i in 1..partnerCount){
        val partner = PartnerDataModel("Partner $i", "Partner $i SIA", "400100$i", "LV-400100$i")
        partner.contact = ContactDataModel(true,"Contact for ${partner.name}", "Some Surname")
        var depCount = departmentCount
        if(randomizeDepartmentCount){
            depCount = Random.nextInt(1, departmentCount)
        }
        for (a in 1..depCount){
            val isHq = a == 1
            val department = DepartmentDataModel(
                isHq,
                "Department $a of ${partner.name}",
                 12, "Some street $a",
                "Riga", "Latvia",
                "LV-190$a",
                "26000$i$a",
                "Department$a@Partner${i}.lv")
            for (b in 1..3){
                department.inspections.add(InspectionDataModel(InspectionDTO.nowTime()))
            }
            partner.departments.add(department)
        }
        result.add(partner)
    }
    return result
}