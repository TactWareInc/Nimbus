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
        val driver = JdbcSqliteDriver("jdbc:sqlite:nimbus.db").also {
            if (!File("nimbus.db").exists()) {
                NimbusDb.Schema.create(it)
            }
        }

        database = NimbusDb.invoke(driver)
    }
}