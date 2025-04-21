package po.restwraptor.builders

import io.ktor.server.application.Application
import po.restwraptor.RestWrapTor


fun restWrapTor(app: Application) : RestWrapTor{
    val wraptor = RestWrapTor()
   // wraptor.usePreconfiguredApp(app)
    return wraptor
}

fun restWrapTor(appBuilderFn: ()-> Application): RestWrapTor{
    return restWrapTor(appBuilderFn.invoke())
}