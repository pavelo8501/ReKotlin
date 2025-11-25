package po.misc.data.pretty_print.presets

import po.misc.data.pretty_print.parts.RenderOptions

interface RendererPresets {

    val outputIntegrity: Boolean

    fun toOptions(): RenderOptions{
       return  RenderOptions(this)
    }

    object OutputIntegrity : RendererPresets{
        override val outputIntegrity: Boolean = true
    }


}