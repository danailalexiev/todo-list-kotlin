package bg.dalexiev.todo.utils

import bg.dalexiev.todo.Environment
import bg.dalexiev.todo.di.Dependencies
import bg.dalexiev.todo.module
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.core.spec.style.scopes.FeatureSpecContainerScope
import io.kotest.core.test.TestScope
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*

abstract class ApiIntegrationTestSpec(body: context(Environment, Dependencies, JdbcTemplate) FeatureSpec.() -> Unit = {}) :
    FeatureSpec({

        val dataSource = install(postgresContainerExtension)

        val environment = Environment(
            dataSource = Environment.DataSource(
                url = dataSource.jdbcUrl,
                user = dataSource.username,
                password = dataSource.password,
                driver = "org.postgresql.Driver"
            )
        )

        val dependencies = Dependencies(environment, dataSource)

        val jdbcTemplate = JdbcTemplate(dataSource)
        
        body(environment, dependencies, jdbcTemplate, this)
    })

context(Environment, Dependencies, JdbcTemplate)
suspend fun FeatureSpecContainerScope.appScenario(
    name: String,
    body: suspend context(HttpClient) TestScope.() -> Unit = {}
) = scenario(name) {
    testApplication {
        application { module(this@Environment, this@Dependencies) }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        body(client, this@scenario)
    }

}
