package po.exposify.test

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import po.exposify.test.setup.tables

abstract class DatabaseTest {
    @BeforeEach
    fun setupDatabase() {
        transaction(TestDatabase.connect()) {
            SchemaUtils.drop(*tables().toTypedArray())
            SchemaUtils.create(*tables().toTypedArray())
        }
    }

    @AfterEach
    fun cleanupDatabase() {
        transaction(TestDatabase.connect()) {
            SchemaUtils.drop(*tables().toTypedArray())
        }
    }
}