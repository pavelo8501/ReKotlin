package po.playground.projects.data_service

import po.db.data_service.DatabaseManager
import po.db.data_service.models.ConnectionModel
import po.playground.projects.data_service.services.PartnerService


class DBManager(connectionData : ConnectionModel) : DatabaseManager(connectionData) {

    override fun init() {
        addService("partner", PartnerService(this))
    }

    fun partnerService() : PartnerService {
        return getService("partner") as PartnerService
    }

}

