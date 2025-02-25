package po.exposify.test

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class DatabaseTest {
    @BeforeTest
    fun setupDatabase() {
        transaction(TestDatabase.connect()) {
            SchemaUtils.drop(*tables().toTypedArray())
            SchemaUtils.create(*tables().toTypedArray())
        }
    }

    @AfterTest
    fun cleanupDatabase() {
        transaction(TestDatabase.connect()) {
            SchemaUtils.drop(*tables().toTypedArray())
        }
    }
}