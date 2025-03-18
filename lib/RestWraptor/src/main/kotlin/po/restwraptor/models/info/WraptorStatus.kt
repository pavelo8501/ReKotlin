package po.restwraptor.models.info

import po.restwraptor.models.server.WraptorRoute

data class WraptorStatus(
    val running: Boolean,
    val listeningConnectors : List<String>,
    val baseUrl: String,
    val production: Boolean,
    val installedPlugins:  List<String>,
    val systemRouts: List<WraptorRoute>,
    val publicRouts: List<WraptorRoute>,
    val securedRouts: List<WraptorRoute>
){



}