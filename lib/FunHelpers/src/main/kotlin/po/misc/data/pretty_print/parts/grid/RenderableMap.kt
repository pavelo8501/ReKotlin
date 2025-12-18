package po.misc.data.pretty_print.parts.grid

import po.misc.collections.putIfAbsentOr
import po.misc.collections.putOverwriting
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.RenderableElement
import po.misc.types.castOrThrow
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.collections.set




class RenderableMap <T: Any>(
    private val hostType: TypeToken<T>,
){

    private var orderCounter = 0
    private val foreignRenderBacking: MutableMap<GridKey, RenderableElement<*>> = mutableMapOf()
    private val renderBacking: MutableMap<GridKey, RenderableElement<T>> = mutableMapOf()

    val foreignMap : Map<GridKey, RenderableElement<*>> = foreignRenderBacking
    val renderMap : Map<GridKey, RenderableElement<T>>  = renderBacking

    val foreignSize : Int get() = foreignRenderBacking.size
    val renderSize : Int get() = renderBacking.size
    val size: Int get() =  foreignSize + renderSize


    val renderables: List<RenderableWrapper> get() = getJoinedRenderables()
    val elements: List<RenderableElement<T>> get() = renderBacking.values.toList()
    val rows: List<PrettyRow<T>> get() = renderBacking.values.filterIsInstance<PrettyRow<T>>()

    private fun nextOrder(): Int = ++orderCounter

    private fun makeThrow(msg: String): Nothing{
        throw IllegalStateException(msg)
    }
    private fun generateKey(element: RenderableElement<*>, ownHostType:Boolean): GridKey {

       return when(element){
            is PrettyGrid<*> -> {
                if(ownHostType){
                    GridKey(nextOrder(), RenderableType.Grid)
                }else{
                    GridKey(nextOrder(), RenderableType.ForeignGrid)
                }
            }
            is PrettyRow<*> ->  GridKey(nextOrder(), RenderableType.Row)
            is PrettyValueGrid<*, *> -> GridKey(nextOrder(), RenderableType.ValueGrid)
        }
    }

    fun addRenderElement(element: RenderableElement<*>):GridKey{
        if(element.hostType == hostType){
           return element.safeCast<RenderableElement<T>>()?.let {casted->
                val key = generateKey(casted, ownHostType = true)
               renderBacking.putOverwriting(key, casted){existent->
                   existent.output("This entry of renderBacking already existed")
               }
               key
            }?:run {
                makeThrow("Tokens align but cast failed. Method: addRenderElement")
            }
        }else{
            val key = generateKey(element, ownHostType = false)
            foreignRenderBacking.putOverwriting(key, element){existent->
                existent.output("This entry of foreignRenderBacking already existed")
            }
            return key
        }
    }
    fun getJoinedRenderables(): List<RenderableWrapper>{
        val ownRenderables = renderMap.entries.map {RenderableWrapper(it.value, it.key.type, it.key.order) }
        val foreignRenderables = foreignMap.entries.map {RenderableWrapper(it.value, it.key.type, it.key.order) }
        val joinedRenderables = buildList {
            addAll(ownRenderables)
            addAll(foreignRenderables)
        }
        return joinedRenderables.sortedBy { it.order }
    }

    fun populateBy(other: RenderableMap<T>){
        foreignRenderBacking.clear()
        other.foreignMap.forEach { (foreignKey, foreignElement) ->
            foreignRenderBacking[foreignKey] = foreignElement
        }
        renderBacking.clear()
        other.renderMap.forEach { (key, element) ->
            renderBacking[key] = element
        }
    }
}