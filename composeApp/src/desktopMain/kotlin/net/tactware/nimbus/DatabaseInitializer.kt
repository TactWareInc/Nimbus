package net.tactware.nimbus

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.tactware.nimbus.appwide.dal.IDatabaseProvider
import net.tactware.nimbus.db.NimbusDb
import org.koin.core.annotation.Single
import java.io.File

@Single(createdAtStart = true, binds = [IDatabaseProvider::class])
class DatabaseInitializer : IDatabaseProvider<NimbusDb> {

    override val database: NimbusDb

    init {
        val dbFile = File("nimbus.db")
        val driver = JdbcSqliteDriver("jdbc:sqlite:nimbus.db")

        // Create the schema if the database file doesn't exist, or migrate if it exists
        if (!dbFile.exists()) {
            NimbusDb.Schema.create(driver)
        } else {
            // Ensure schema is up-to-date by migrating
            NimbusDb.Schema.migrate(driver, 0, NimbusDb.Schema.version)
        }

        database = NimbusDb.invoke(driver)
    }
}
