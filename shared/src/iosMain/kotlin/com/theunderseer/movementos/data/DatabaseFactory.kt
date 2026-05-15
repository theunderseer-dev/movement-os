package com.theunderseer.movementos.data

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.theunderseer.movementos.database.MovementOSDatabase

actual class DatabaseFactory {
    actual fun create(): MovementOSDatabase {
        val driver = NativeSqliteDriver(MovementOSDatabase.Schema, DATABASE_NAME)
        return MovementOSDatabase(driver)
    }

    private companion object {
        const val DATABASE_NAME = "movement_os.db"
    }
}
