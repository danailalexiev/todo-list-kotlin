package bg.dalexiev.todo.di

import bg.dalexiev.todo.Environment
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

fun hikari(environment: Environment): HikariDataSource =
    HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = environment.dataSource.url
            username = environment.dataSource.user
            password = environment.dataSource.password
            driverClassName = environment.dataSource.driver
        }
    )