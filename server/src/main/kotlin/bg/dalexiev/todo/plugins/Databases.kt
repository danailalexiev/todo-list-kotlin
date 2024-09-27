package bg.dalexiev.todo.plugins

import bg.dalexiev.todo.user.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

fun configureDatabases(dataSource: DataSource) {
    Database.connect(dataSource)
        .also {
            transaction(it) {
                SchemaUtils.create(Users)
            }
        }
}
