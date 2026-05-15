package com.theunderseer.movementos.data

import com.theunderseer.movementos.database.MovementOSDatabase

/**
 * Platform-specific database driver factory.
 *
 * Android: AndroidSqliteDriver with app context.
 * iOS: NativeSqliteDriver.
 */
expect class DatabaseFactory {
    fun create(): MovementOSDatabase
}
