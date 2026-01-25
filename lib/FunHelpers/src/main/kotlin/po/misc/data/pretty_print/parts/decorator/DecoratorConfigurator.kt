package po.misc.data.pretty_print.parts.decorator

import po.misc.data.logging.Verbosity

class DecoratorConfigurator(
    internal val decorator: Decorator
){
    var policy: DecorationPolicy by decorator::policy
    var verbosity: Verbosity by decorator::verbosity
    var onBordersApplied: ((DecorationContent) -> Unit)?  by decorator::onBordersApplied

    fun addSeparator(vararg taggedSeparators: po.misc.data.pretty_print.parts.common.TaggedSeparator<BorderPosition>){
        taggedSeparators.forEach {
            decorator.addSeparator(it)
        }
    }

    fun config(direction:  BorderPosition, configAction: DecoratorBorder.()-> Unit ){
        configAction.invoke(decorator[direction])
    }
    fun configTop(configAction: DecoratorBorder.()-> Unit): Unit = config(BorderPosition.Top, configAction)
    fun configBottom(configAction: DecoratorBorder.()-> Unit): Unit = config(BorderPosition.Bottom, configAction)
    fun configLeft(configAction: DecoratorBorder.()-> Unit): Unit = config(BorderPosition.Left, configAction)
    fun configRight(configAction: DecoratorBorder.()-> Unit): Unit = config(BorderPosition.Right, configAction)
}