package com.example.simple_english

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseUsage {
    private val host = System.getenv("DB_HOST") ?: "localhost"
    private val port = System.getenv("DB_PORT")?.toIntOrNull() ?: 5432
    private val dbName = System.getenv("DB_NAME") ?: "user_db"
    private val dbUser = System.getenv("DB_USER") ?: "postgres"
    private val dbPassword = ""

    object Users : Table() {
        val id = integer("id").primaryKey().autoIncrement()
        val username = text("username")
        val password = text("password")
    }

    fun connectToDatabase() {
        Database.connect(
            url = "jdbc:postgresql://$host:$port/$dbName",
            driver = "org.postgresql.Driver",
            user = dbUser,
            password = dbPassword,
        )

        transaction {
            SchemaUtils.create(Users)

            Users.insert {
                it[username] = "test"
                it[password] = "test"
            }

            for (user in Users.selectAll()) {
                println("${user[Users.id]}")
            }
        }
    }
}