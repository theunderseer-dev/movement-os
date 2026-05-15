package com.theunderseer.movementos.data

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.theunderseer.movementos.database.MovementOSDatabase

actual class DatabaseFactory(
    private val context: Context,
) {
    actual fun create(): MovementOSDatabase {
        val driver =
            AndroidSqliteDriver(
                schema = MovementOSDatabase.Schema,
                context = context,
                name = DATABASE_NAME,
            )
        return MovementOSDatabase(driver)
    }

    private companion object {
        const val DATABASE_NAME = "movement_os.db"
    }
}
