package po.playground.projects.data_service

import po.db.data_service.DatabaseManager
import po.db.data_service.models.ConnectionModel
import po.playground.projects.data_service.services.DepartmentService
import po.playground.projects.data_service.services.PartnerService


class DBManager(connectionData : ConnectionModel) : DatabaseManager(connectionData) {

    override fun init() {
        addService("partner", PartnerService(this))
        addService("department", DepartmentService(this))
    }

    fun partnerService() : PartnerService {
        return getService("partner") as PartnerService
    }

    fun departmentService() : DepartmentService {
        return getService("department") as DepartmentService
    }

}

