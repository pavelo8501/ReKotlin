package po.misc.lookups

import po.misc.types.TypeRecord


data class HierarchyNode<T: Any> (
    val type: TypeRecord<*>,
    val items: List<T>,
){
    val children: MutableList<HierarchyNode<out T>> = mutableListOf()

    fun addChildNode(node: HierarchyNode<out T>):HierarchyNode<T>{
        children.add(node)
        return this
    }
    fun addChildNodes(nodes: List<HierarchyNode<out T>>):HierarchyNode<T>{
        children.addAll(nodes)
        return this
    }
}

fun <T : Any, R : Any> transformNode(source:  HierarchyNode<T>,   transform: (T) -> R): HierarchyNode<R> {
    val newItems = source.items.map(transform)

    val newChildren = source.children.map { transformNode<T,R>(it as HierarchyNode<T>, transform) }.toMutableList()
    return HierarchyNode(source.type, newItems).apply {
        addChildNodes(newChildren)
    }
}
