package bg.dalexiev.todo.core.db

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpsertStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface CrudRepository<E, ID> {

    suspend fun save(entity: E): E

    suspend fun findById(id: ID): E?

    suspend fun findAll(): List<E>

    suspend fun delete(entity: E)

}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }

class ExposedCrudRepository<E, ID : Any, T : Table>(
    private val table: T,
    private val getId: (E) -> ID,
    private val entityToRow: T.(UpsertStatement<Long>, E) -> Unit,
    private val rowToEntity: (ResultRow) -> E,
    private val idPredicate: (ID) -> Op<Boolean>
) : CrudRepository<E, ID> {

    override suspend fun save(entity: E): E = dbQuery {
        table.upsert {
            entityToRow(it, entity)
        }
    }.resultedValues?.firstNotNullOfOrNull { rowToEntity(it) } ?: error("Could not save entity")

    override suspend fun findById(id: ID): E? = dbQuery {
        table.selectAll()
            .where { idPredicate(id) }
            .singleOrNull()
    }?.let { rowToEntity(it) }

    override suspend fun findAll(): List<E> = dbQuery {
        table.selectAll().toList().map { rowToEntity(it) }
    }

    override suspend fun delete(entity: E) {
        dbQuery {
            val id = getId(entity)
            table.deleteWhere { idPredicate(id) }
        }
    }
}
