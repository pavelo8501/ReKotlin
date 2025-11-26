package po.misc.data.pretty_print.presets

import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.RenderOptions

interface RendererPresets {

    val orientation: Orientation
    val outputIntegrity: Boolean

    fun toOptions(): RenderOptions{
       return  RenderOptions(this)
    }

    object OutputIntegrity : RendererPresets{
        
        override val orientation: Orientation = Orientation.Horizontal
        override val outputIntegrity: Boolean = true
    }


}