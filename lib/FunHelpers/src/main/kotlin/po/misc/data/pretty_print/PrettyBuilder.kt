package po.misc.data.pretty_print

import po.misc.data.pretty_print.cells.StaticCell

interface PrettyBuilder {

    fun String.toStatic(): StaticCell{
      return  StaticCell(this)
    }

}