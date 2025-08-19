package po.exposify.common.classes

import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.models.ConnectionInfo
import po.misc.exceptions.ManagedException


class DBManagerHooks {
    internal var onBeforeConnection: (() -> ConnectionInfo?)? = null
    internal var onNewConnection: ((ConnectionClass) -> Unit)? = null
    internal var onExistentConnection: ((ConnectionClass) -> Unit)? = null
    internal var onConnectionFail: ((Throwable) -> Unit)? = null


    fun beforeConnection(hook: () -> ConnectionInfo?) {
        onBeforeConnection = hook
    }
    fun newConnection(hook: (ConnectionClass) -> Unit) {
        onNewConnection = hook
    }
    fun existentConnection(hook: (ConnectionClass) -> Unit) {
        onExistentConnection = hook
    }

    fun connectionFail(hook: (Throwable) -> Unit){
        onConnectionFail = hook
    }

}

fun dbHooks(configure: DBManagerHooks.() -> Unit): DBManagerHooks {
    return DBManagerHooks().apply(configure)
}

