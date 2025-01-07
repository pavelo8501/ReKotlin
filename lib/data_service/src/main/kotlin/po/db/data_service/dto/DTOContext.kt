package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.interfaces.CanNotifyDepr
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.controls.Notificator
import po.db.data_service.dto.components.DTOConfig

class DTOContext<DATA>(

) : CanNotifyDepr where DATA : DataModel {

   override val name: String =  "DTOContext"

   override val  notificator: Notificator = Notificator(this)

    init {
        println("DTOContext| $name Initialized")
    }

}