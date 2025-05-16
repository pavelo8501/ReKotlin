package po.exposify.dto.interfaces

interface IdentifiableComponent {
    val qualifiedName: String
    val type: ComponentType
}


enum class ComponentType{
    Factory,
    DaoService,
    SingleRepository,
    MultipleRepository,
    RootExecutionProvider,
    SequenceContext,
    ResponsiveDelegate

}