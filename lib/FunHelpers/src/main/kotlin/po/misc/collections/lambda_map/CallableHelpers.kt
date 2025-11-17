package po.misc.collections.lambda_map

import po.misc.context.tracable.TraceableContext
import po.misc.functions.LambdaOptions
import po.misc.functions.SuspendedOptions


fun <T : Any, R> Function1<T, R>.toCallable(
    options: LambdaOptions
):Lambda<T, R> = Lambda( options, this )

fun <T : Any, R> Function1<T, R>.toCallable(
):Lambda<T, R> = Lambda( LambdaOptions.Listen, this )


fun <T : Any, R> TraceableContext.toCallable(
    function: suspend (T) -> R
):SuspendingLambda<T, R> =  SuspendingLambda(SuspendedOptions.Listen, function)

fun <T : Any, R> TraceableContext.toCallable(
    options: SuspendedOptions,
    function: suspend (T) -> R
):SuspendingLambda<T, R> =  SuspendingLambda(options, function)


fun <H : TraceableContext, T : Any, R> Function2<H, T, R>.toCallable(
    receiver:H,
    options: LambdaOptions
):LambdaWithReceiver<H, T, R> = LambdaWithReceiver(receiver, options, this)

fun <H : TraceableContext, T : Any, R> Function2<H, T, R>.toCallable(
    receiver:H,
):LambdaWithReceiver<H, T, R> = LambdaWithReceiver(receiver, LambdaOptions.Listen, this)


fun <H : TraceableContext, T : Any, R>  H.toCallable(
    options: SuspendedOptions,
    function: suspend H.(T) -> R
):SuspendingLambdaWithReceiver<H, T, R> = SuspendingLambdaWithReceiver(this, options, function)

fun <H : TraceableContext, T : Any, R>  H.toCallable(
    function: suspend H.(T) -> R
):SuspendingLambdaWithReceiver<H, T, R> = SuspendingLambdaWithReceiver(this, SuspendedOptions.Listen, function)



