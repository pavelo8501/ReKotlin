package po.exposify.dto.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrOperations
import po.misc.interfaces.ValueBased
import po.misc.types.TypeRecord
import po.misc.types.safeCast


sealed class SourceObject<T: Any>(override val value: Int): ValueBased{
    abstract var name: String
    internal var typeRecord : TypeRecord<T>? = null

    fun getTypeRecord(): TypeRecord<T>{
        return typeRecord.getOrOperations()
    }

    object DTO : SourceObject<ModelDTO>(1){
        override var name: String = "DTO"
        fun <T: ModelDTO> provideType(record : TypeRecord<T>): DTO {
            typeRecord = record.safeCast()
            name = record.simpleName
            return this
        }
    }

    object Data : SourceObject<DataModel>(2){
        override var name: String = "Data"
        fun <T: DataModel> provideType(record : TypeRecord<T>): Data {
            typeRecord = record.safeCast()
            name = record.simpleName
            return this
        }
    }

    object Entity : SourceObject<LongEntity>(3){
        override var name: String = "entity"
        fun <T: LongEntity> provideType(record : TypeRecord<T>): Entity {
            typeRecord = record.safeCast()
            name = record.simpleName
            return this
        }
    }

    object CommonDTOType : SourceObject<CommonDTO<*, *, *>>(4){
        override var name: String = "commonDTO"
        fun <T: CommonDTO<* ,* , *>> provideType(record : TypeRecord<T>): CommonDTOType {
            typeRecord = record.safeCast()
            name = record.simpleName
            return this
        }
    }
}