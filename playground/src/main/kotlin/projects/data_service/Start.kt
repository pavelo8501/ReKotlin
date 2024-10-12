package po.playground.projects.data_service

import po.db.data_service.models.ConnectionModel
import po.playground.projects.data_service.services.models.PartnerModel

fun startDataService(connectionData : ConnectionModel) {
    val dbManager = DBManager(connectionData)

    val new = PartnerModel(0, "Partner1", "Partner1 SIA")

    val result = dbManager.partnerService().initDataModel(new)

    val a  = result
}