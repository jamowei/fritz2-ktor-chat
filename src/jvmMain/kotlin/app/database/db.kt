package app.database

import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction


//TODO: Add Repos

fun <T> database(statement: Transaction.() -> T): T {
    return transaction {
        addLogger(StdOutSqlLogger)
        statement()
    }
}