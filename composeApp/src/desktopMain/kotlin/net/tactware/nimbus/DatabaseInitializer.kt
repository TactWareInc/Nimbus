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
            try {
                // Try to migrate from the current version to the latest version
                // If this fails, we'll catch the exception and create the schema
                NimbusDb.Schema.migrate(driver, 0, NimbusDb.Schema.version)
            } catch (e: Exception) {
                // If migration fails, log the error and try to create the schema
                println("Migration failed: ${e.message}")
                try {
                    // Try to create the schema
                    NimbusDb.Schema.create(driver)
                } catch (e2: Exception) {
                    // If creating the schema also fails, log the error and rethrow
                    println("Failed to create schema: ${e2.message}")
                    throw e2
                }
            }
        }

        database = NimbusDb.invoke(driver)
    }
}
