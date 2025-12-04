package po.misc.data.pretty_print.parts



class GridOptions(
    vararg val excludeRender : Enum<*>
){
    constructor(renderOnlyIds: Collection< Enum<*>>):this(){
        renderOnly = renderOnlyIds.toList()
    }
    val exclusionList: List<String> = excludeRender.toList().map { it.name }
    var renderOnly: List<Enum<*>> = emptyList()
}