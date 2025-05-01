package po.exposify.dto.components.relation_binder

import po.exposify.classes.DTOBase
import po.exposify.dto.enums.Cardinality
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity

sealed class BindingKeyBase(val ordinance: Cardinality) {

    open class  OneToMany<DTO: ModelDTO>(
        val dtoClass :DTOBase<DTO, *>
    ):BindingKeyBase(Cardinality.ONE_TO_MANY)

    open class  OneToOne<DTO: ModelDTO>(
        val dtoClass : DTOBase<DTO, *>
    ):BindingKeyBase(Cardinality.ONE_TO_ONE)

    open class  ManyToMany<DTO: ModelDTO>(
        val dtoClass : DTOBase<DTO, *>
    ):BindingKeyBase(Cardinality.MANY_TO_MANY)

    companion object{
        fun <DTO: ModelDTO> createOneToManyKey(
            dtoClass : DTOBase<DTO, *>
        ): OneToMany<DTO>{
            return  OneToMany(dtoClass)

        }

        fun <DTO: ModelDTO> createOneToOneKey(
            dtoClass : DTOBase<DTO, *>
        ): OneToOne<DTO>{
            return  object : OneToOne<DTO>(dtoClass) {}
        }
    }
}