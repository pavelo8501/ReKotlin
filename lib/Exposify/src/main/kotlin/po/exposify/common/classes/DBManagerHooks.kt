package po.exposify.common.classes

import po.exposify.scope.connection.ConnectionClass


class DBManagerHooks {
    internal var onBeforeConnection: (() -> Unit)? = null
    internal var onNewConnection: ((ConnectionClass) -> Unit)? = null
    internal var onExistentConnection: ((ConnectionClass) -> Unit)? = null

    fun beforeConnection(hook: () -> Unit) {
        onBeforeConnection = hook
    }

    fun newConnection(hook: (ConnectionClass) -> Unit) {
        onNewConnection = hook
    }

    fun existentConnection(hook: (ConnectionClass) -> Unit) {
        onExistentConnection = hook
    }
}

fun dbHooks(configure: DBManagerHooks.() -> Unit): DBManagerHooks {
    return DBManagerHooks().apply(configure)
}

