package org.biblestudio.test

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.biblestudio.database.BibleStudioDatabase

/**
 * Creates an in-memory [BibleStudioDatabase] for unit tests.
 * The caller is responsible for closing the returned driver
 * via [TestDatabase.close].
 */
class TestDatabase {
    val driver: JdbcSqliteDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    val database: BibleStudioDatabase

    init {
        BibleStudioDatabase.Schema.create(driver)
        database = BibleStudioDatabase(driver)
    }

    fun close() {
        driver.close()
    }
}
