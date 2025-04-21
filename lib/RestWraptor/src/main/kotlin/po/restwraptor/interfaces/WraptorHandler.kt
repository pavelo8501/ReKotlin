package po.restwraptor.interfaces

import po.restwraptor.models.server.WraptorRoute

interface WraptorHandler {
    fun stop(gracePeriod: Long = 5000)
    fun getRoutes():List<WraptorRoute>
}