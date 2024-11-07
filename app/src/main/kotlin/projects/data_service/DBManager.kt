package po.playground.projects.data_service

import po.db.data_service.DatabaseManager
import po.db.data_service.models.ConnectionModel
import po.db.data_service.services.BaseService
import po.playground.projects.data_service.services.DepartmentService

class DBManager(connectionData : ConnectionModel) : DatabaseManager(connectionData) {


    init {

    }

    override fun init() {
        addService("department", DepartmentService(this))
    }

//    fun partnerService() : PartnerService {
//        return getService("partner") as PartnerService
//    }

    fun departmentService() : DepartmentService {
        return getService("department") as DepartmentService
    }

}

