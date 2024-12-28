package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

class DTORepo(
    //val forProperty: KProperty1<PARENT, SizedIterable<CHILD>>,
    //val childModel: DTOClass<CHILD>,
)  {

    private val entities = mutableListOf<CommonDTO>()

    fun add(dto:CommonDTO){
        entities.add(dto)
    }


}