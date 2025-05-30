package po.exposify.dto.interfaces

import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased

interface IdentifiableComponent : Identifiable {
    override val qualifiedName: String
    val type: ComponentType
}


enum class ComponentType(override val value: Int): ValueBased{
    Factory(1),
    DaoService(2),
    RootExecutionProvider(5),
    SequenceContext(6),
    ResponsiveDelegate(7),
    RelationBindingDelegate(8),
    ServiceClass(9),
    DTO(10),
    DATA_MODEL(11),
    ENTITY(12),
    DTO_Class(13),
    ComplexDelegate(14);
}