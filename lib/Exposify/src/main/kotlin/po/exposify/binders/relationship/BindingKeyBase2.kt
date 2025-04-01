package po.exposify.binders.relationship

import po.exposify.binders.enums.Cardinality
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.interfaces.ModelDTO

sealed class BindingKeyBase2(val ordinance: Cardinality) {
    open class  OneToMany<CHILD_DTO: ModelDTO>(
        val childModel :DTOClass2<CHILD_DTO>
    ):BindingKeyBase2(Cardinality.ONE_TO_MANY)
    open class  OneToOne<CHILD_DTO: ModelDTO>(
        val childModel : DTOClass2<CHILD_DTO>
    ):BindingKeyBase2(Cardinality.ONE_TO_ONE)
    open class  ManyToMany<CHILD_DTO: ModelDTO>(
        val childModel : DTOClass2<CHILD_DTO>
    ):BindingKeyBase2(Cardinality.MANY_TO_MANY)
    companion object{
        fun <CHILD_DTO: ModelDTO> createOneToManyKey(
            childModel : DTOClass2<CHILD_DTO>
        ): OneToMany<CHILD_DTO>{
            return  OneToMany<CHILD_DTO>( childModel)

        }

        fun <CHILD_DTO: ModelDTO> createOneToOneKey(
            childModel : DTOClass2<CHILD_DTO>
        ): OneToOne<CHILD_DTO>{
            return  object : OneToOne<CHILD_DTO>(childModel) {}
        }
    }
}