package po.exposify.dto.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dao.helpers.getExposifyEntityCompanion
import po.exposify.dao.models.TableColumnMap
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.interfaces.Copyable
import po.misc.types.TypeData
import po.misc.types.Typed
import po.misc.types.containers.ThreeTypeRegistry
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


class EntityType<E: LongEntity>(
    kClass: KClass<E>,
    kType: KType,
    val entityClass: ExposifyEntityClass<E>
): TypeData<E>(kClass, kType){


    var  tableColumnMap: TableColumnMap =  TableColumnMap()

    val isInitialized: Boolean get() = tableColumnMap.propertySize > 0

    fun provideColumnMetadata(columnMap:  TableColumnMap){
        tableColumnMap = columnMap
    }

    fun copy():EntityType<E>{
        return EntityType(kClass, kType, entityClass).also {
            it.tableColumnMap = tableColumnMap.copy()
        }
    }

    companion object{
        inline fun <reified E: LongEntity> create(
            entityClass: ExposifyEntityClass<E>
        ):EntityType<E>{
           return EntityType(E::class, typeOf<E>(), entityClass)
        }
    }
}

class CommonDTOType<DTO: ModelDTO, D: DataModel,  E: LongEntity>(
    val dtoType: TypeData<DTO>,
    val dataType: TypeData<D>,
    val entityType: EntityType<E>,
    val commonType: TypeData<CommonDTO<DTO,  D, E>>,
    private val dtoBaseType:TypeData<DTOBase<DTO, D, E>>,

): ThreeTypeRegistry<DTO, D, E>(dtoType, dataType, entityType), Copyable<CommonDTOType<DTO, D, E>>{

    val baseKClass: KClass<DTOBase<DTO, D, E>> get() = dtoBaseType.kClass
    val baseKType: KType get() = dtoBaseType.kType

    fun initializeColumnMetadata(){
       val propertyMap =  entityType.entityClass.createTablePropertyMap()
       entityType.provideColumnMetadata(propertyMap)
    }

    override fun copy(): CommonDTOType<DTO, D, E> {
        val newEntityType = entityType.copy()
        return CommonDTOType(dtoType, dataType, newEntityType, commonType,  dtoBaseType)
    }

    override fun toString(): String = "CommonDTOType<${dtoType.typeName}, ${dataType.typeName}, ${entityType.typeName}>"


    companion object {
        inline fun <reified DTO, reified D, reified E> create(
            dtoClass: DTOBase<DTO, D, E>
        ): CommonDTOType<DTO, D, E> where DTO : ModelDTO, D : DataModel, E : LongEntity {

            val entityClass = getExposifyEntityCompanion<E>(this)
            val entityType = EntityType.create<E>(entityClass)
            val dtoBaseType: TypeData<DTOBase<DTO, D, E>> = TypeData.create()
            println("DTOBaseType ${dtoBaseType.typeName}")
            val commonType = TypeData.create<CommonDTO<DTO, D, E>>()
            return CommonDTOType(
                dtoType = TypeData.create<DTO>(),
                dataType = TypeData.create<D>(),
                entityType = entityType,
                commonType = commonType,
                dtoBaseType = dtoBaseType
            )
        }
    }

}