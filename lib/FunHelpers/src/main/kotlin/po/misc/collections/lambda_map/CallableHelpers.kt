package po.misc.collections.lambda_map

import po.misc.context.tracable.TraceableContext
import po.misc.functions.LambdaOptions
import po.misc.functions.SuspendedOptions

fun <T: Any, R> Function1<T, R>.toCallable():Lambda<T, R>{
    return Lambda( LambdaOptions.Listen, this)
}

fun <T: Any, R> Function1<T, R>.toCallable(options: LambdaOptions):Lambda<T, R>{
    return Lambda(options, this)
}

fun <T, R> TraceableContext.toCallable(
    options: SuspendedOptions,
    function: suspend (T)->R
):SuspendingLambda<T, R>{
    return SuspendingLambda(options, function)
}

fun <T, R> TraceableContext.toCallable(function: suspend (T)->R):SuspendingLambda<T, R>{
    return SuspendingLambda(SuspendedOptions.Listen, function)
}

fun <H: TraceableContext, T: Any, R> Function2<H, T, R>.toCallable(receiver:H):LambdaWithReceiver<H, T, R>{
    return LambdaWithReceiver(receiver, LambdaOptions.Listen, this)
}


fun <H: TraceableContext, T: Any, R> Function2<H, T, R>.toCallable(receiver:H, options: LambdaOptions):LambdaWithReceiver<H, T, R>{
    return LambdaWithReceiver(receiver, options, this)
}


fun <H: TraceableContext, T: Any, R>  H.toCallable(
    function: suspend H.(T)->R
):SuspendingLambdaWithReceiver<H, T, R>{
    return SuspendingLambdaWithReceiver(this, SuspendedOptions.Listen, function)
}

fun <H: TraceableContext, T: Any, R>  H.toCallable(
    options: SuspendedOptions,
    function: suspend H.(T)->R
):SuspendingLambdaWithReceiver<H, T, R>{
    return SuspendingLambdaWithReceiver(this, options, function)
}