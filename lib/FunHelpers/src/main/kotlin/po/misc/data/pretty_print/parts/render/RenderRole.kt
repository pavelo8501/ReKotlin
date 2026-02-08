package po.misc.data.pretty_print.parts.render

import po.misc.data.text_span.SpanRole
import po.misc.data.text_span.TextSpan

sealed interface RenderRole :SpanRole{
    val ordinal: Int

    object TopBorder: RenderRole{
        override val ordinal: Int = 0
    }
    object Content: RenderRole{
        override val ordinal: Int = 1
    }
    object BottomBorder: RenderRole{
        override val ordinal: Int = 2
    }

    companion object{
        val roles:  List<RenderRole> = listOf(TopBorder, Content, BottomBorder)
    }
}

enum class LayerType { Dynamic,  Render, Decoration }

internal fun TextSpan.hasRoleBorder(): Boolean{
   return when(val span = this){
        is CanvasLayer -> {
            span.lines.hasRoleBorder()
        }
        is TextSpan -> span.role is RenderRole.BottomBorder || span.role is RenderRole.TopBorder
    }
}

internal fun List<TextSpan>.hasRoleBorder(): Boolean{
    return any { it.hasRoleBorder() }
}

