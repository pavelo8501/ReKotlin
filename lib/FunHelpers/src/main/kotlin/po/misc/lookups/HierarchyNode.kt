package po.misc.lookups

import po.misc.types.token.TypeToken


data class HierarchyNode<T: Any> (
    val type: TypeToken<*>,
    val items: List<T>,
){
    val children: MutableList<HierarchyNode<*>> = mutableListOf()
    fun addChildNode(node: HierarchyNode<*>):HierarchyNode<T>{
        children.add(node)
        return this
    }
    fun addChildNodes(nodes: List<HierarchyNode<*>>):HierarchyNode<T>{
        children.addAll(nodes)
        return this
    }
}

fun <T : Any, R : Any> transformNode(source:  HierarchyNode<T>,   transform: (T) -> R): HierarchyNode<R> {
    val newItems = source.items.map(transform)
    @Suppress("UNCHECKED_CAST")
    val newChildren = source.children.map { transformNode<T,R>(it as HierarchyNode<T>, transform) }.toMutableList()
    return HierarchyNode(source.type, newItems).apply {
        addChildNodes(newChildren)
    }
}
