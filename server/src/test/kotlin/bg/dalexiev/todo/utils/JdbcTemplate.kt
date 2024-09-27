package bg.dalexiev.todo.utils

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import javax.sql.DataSource

class JdbcTemplate(dataSource: DataSource) : AutoCloseable {

    private val connection = dataSource.connection

    fun <T> queryForObject(query: String, params: List<Any> = emptyList(), mapper: (ResultSet) -> T): T? {
        val results = queryForObjects(query, params, mapper)
        return when {
            results.isEmpty() -> null
            results.size == 1 -> results.first()
            else -> throw SQLException("Found ${results.size} results for query $query")
        }
    }

    fun <T> queryForObjects(query: String, params: List<Any> = emptyList(), mapper: (ResultSet) -> T): List<T> =
        executeStatement(
            query = query,
            params = params,
            executor = { it.executeQuery() },
            mapper = mapper
        )

    fun <ID> insert(query: String, params: List<Any> = emptyList(), mapper: (ResultSet) -> ID): ID? {
        val executor: (PreparedStatement) -> ResultSet = {
            it.executeUpdate()
            it.generatedKeys
        }
     
        val generatedKeys = executeStatement(query, true, params, executor, mapper)
        return when {
            generatedKeys.isEmpty() -> null
            generatedKeys.size == 1 -> generatedKeys.first()
            else -> throw SQLException("Generated ${generatedKeys.size} keys for query $query")
        }
    }

    private fun <T> executeStatement(
        query: String,
        returnGeneratedKeys: Boolean = false,
        params: List<Any> = emptyList(),
        executor: (PreparedStatement) -> ResultSet,
        mapper: (ResultSet) -> T
    ): List<T> {
        val statement = if (returnGeneratedKeys) connection.prepareStatement(
            query,
            Statement.RETURN_GENERATED_KEYS
        ) else connection.prepareStatement(query)
        statement.use {
            params.forEachIndexed { index, param -> statement.setObject(index + 1, param) }
            val resultSet = executor(it)
            resultSet.use {
                val results = mutableListOf<T>()
                while (resultSet.next()) {
                    results.add(mapper(resultSet))
                }
                return results
            }
        }
    }

    override fun close() {
        try {
            connection?.close()
        } catch (e: SQLException) {
            // ignore
        }
    }


}