package po.playground.projects.data_service

import po.db.data_service.models.ConnectionModel
import po.playground.projects.data_service.services.models.DepartmentModel
import po.playground.projects.data_service.services.models.PartnerModel

fun startDataService(connectionData : ConnectionModel) {
    val dbManager = DBManager(connectionData)

    val partner = PartnerModel(0, "Partner1", "Partner1 SIA")

    dbManager.baseService.initContainerModel(partner)
   // partner.save()
    partner.create()


  //  val department1 = DepartmentModel(0, 0, true, "Dep1",36)
   // val department2 = DepartmentModel(0, 0, false, "Dep2",12)
   //partner.departments.add(department1)
   // partner.departments.add(department2)

   // val result = dbManager.partnerService().saveModel(partner)


}