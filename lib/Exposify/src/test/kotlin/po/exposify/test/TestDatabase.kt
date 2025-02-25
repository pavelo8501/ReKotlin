package po.exposify.test

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object TestDatabase {
    private val db: Database by lazy {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MySQL",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ).also {
            transaction(it) {
                SchemaUtils.createMissingTablesAndColumns() // Create tables
            }
        }
    }
    fun connect(): Database = db
}