package bg.dalexiev.todo

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

private const val DEFAULT_HOST = "0.0.0.0"
private const val DEFAULT_PORT = 8080

private const val DEFAULT_AUTH_SECRET = "some secret"
private const val DEFAULT_AUTH_ISSUER = "todo-list-kotlin-server"
private const val DEFAULT_AUTH_DURATION_IN_DAYS = 30

private const val DEFAULT_JDBC_URL = "jdbc:postgresql://localhost:5432/todo_db"
private const val DEFAULT_JDBC_USER = "postgres"
private const val DEFAULT_JDBC_PASSWORD = "password"
private const val DEFAULT_JDBC_DRIVER = "org.postgresql.Driver"

data class Environment(
    val http: Http = Http(),
    val auth: Auth = Auth(),
    val dataSource: DataSource = DataSource()
) {

    data class Http(
        val host: String = System.getenv("HOST") ?: DEFAULT_HOST,
        val port: Int = System.getenv("PORT")?.toIntOrNull() ?: DEFAULT_PORT
    )

    data class Auth(
        val secret: String = System.getenv("JWT_SECRET") ?: DEFAULT_AUTH_SECRET,
        val issuer: String = System.getenv("JWT_ISSUER") ?: DEFAULT_AUTH_ISSUER,
        val duration: Duration = (System.getenv("JWT_DURATION")?.toIntOrNull() ?: DEFAULT_AUTH_DURATION_IN_DAYS).days
    )

    data class DataSource(
        val url: String = System.getenv("DATABASE_URL") ?: DEFAULT_JDBC_URL,
        val user: String = System.getenv("DATABASE_USER") ?: DEFAULT_JDBC_USER,
        val password: String = System.getenv("DATABASE_PASSWORD") ?: DEFAULT_JDBC_PASSWORD,
        val driver: String = System.getenv("DATABASE_DRIVER") ?: DEFAULT_JDBC_DRIVER
    )
}