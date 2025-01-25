package po.restwraptor.resources

import io.ktor.resources.Resource


@Resource("/resource")
class RouteResource(val sort: String? = "new")