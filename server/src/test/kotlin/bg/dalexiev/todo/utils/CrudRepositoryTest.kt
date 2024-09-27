package bg.dalexiev.todo.utils

import bg.dalexiev.todo.core.db.CrudRepository
import bg.dalexiev.todo.plugins.configureDatabases
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

interface EntityOps<E, ID> {

    fun createNewEntity(id: ID): E

    fun generateNewId(): ID
}

interface DatabaseOps<E, ID> {

    fun findEntityById(id: ID): E?

    fun insertEntity(entity: E): ID
    
}

abstract class CrudRepositorySpec<E, ID, R : CrudRepository<E, ID>, DO : DatabaseOps<E, ID>>(
    repo: R,
    entityOps: EntityOps<E, ID>,
    databaseOpsFactory: (JdbcTemplate) -> DO,
    body: context(R, EntityOps<E, ID>, DO) FunSpec.() -> Unit = { }
) : FunSpec({

    val dataSource = install(postgresContainerExtension).also { configureDatabases(it) }

    val jdbcTemplate = JdbcTemplate(dataSource)
    val databaseOps = databaseOpsFactory(jdbcTemplate)

    afterSpec {
        jdbcTemplate.close()
    }

    context("save") {
        test("inserts entity if not saved") {
            // given
            val id = entityOps.generateNewId()
            val entity = entityOps.createNewEntity(id)

            // when
            repo.save(entity)

            // then
            val actualResult = databaseOps.findEntityById(id)
            actualResult shouldBe entity
        }

        test("updates entity if saved") {
            // given
            val id = entityOps.generateNewId()
            val entity = entityOps.createNewEntity(id)
            databaseOps.insertEntity(entity)


            // when
            repo.save(entity)

            // then
            val actualResult = databaseOps.findEntityById(id)
            actualResult shouldBe entity
        }
    }

    context("findById") {
        test("returns entity with matching id") {
            // given
            val id = entityOps.generateNewId()
            val entity = entityOps.createNewEntity(id)
            databaseOps.insertEntity(entity)

            // when
            val actualResult = repo.findById(id)

            // then
            actualResult shouldBe entity
        }

        test("returns null when no entities inserted") {
            // given
            val id = entityOps.generateNewId()

            // when
            val actualResult = repo.findById(id)

            // then
            actualResult shouldBe null
        }

        test("returns null when no matching id") {
            // given
            val id = entityOps.generateNewId()
            val entity = entityOps.createNewEntity(id)
            databaseOps.insertEntity(entity)

            // when
            val actualResult = repo.findById(entityOps.generateNewId())

            // then
            actualResult shouldBe null
        }
    }

    context("findAll") {
        test("returns all entities") {
            // given
            val entities = IntRange(1, 5).map {
                val id = entityOps.generateNewId()
                val entity = entityOps.createNewEntity(id)
                databaseOps.insertEntity(entity)
                entity
            }

            // when
            val actualResult = repo.findAll()

            // then
            actualResult shouldBe entities
        }

        test("returns a single entity if only one inserted") {
            // given
            val id = entityOps.generateNewId()
            val entity = entityOps.createNewEntity(id)
            databaseOps.insertEntity(entity)

            // when
            val actualResult = repo.findAll()

            // then
            actualResult shouldBe listOf(entity)
        }

        test("returns empty list") {
            // when
            val actualResult = repo.findAll()

            // then
            actualResult shouldBe emptyList()
        }
    }

    context("delete") {
        test("removes entity with correct id") {
            // given
            val id = entityOps.generateNewId()
            val entity = entityOps.createNewEntity(id)

            // when
            repo.delete(entity)

            // then
            val actualResult = databaseOps.findEntityById(id)
            actualResult shouldBe null
        }

        test("does not remove entity if id not matching") {
            // given
            val idToBeDeleted = entityOps.generateNewId()
            val entityToBeDeleted = entityOps.createNewEntity(idToBeDeleted)
            databaseOps.insertEntity(entityToBeDeleted)

            val id = entityOps.generateNewId()
            val entityToStay = entityOps.createNewEntity(id)
            databaseOps.insertEntity(entityToStay)

            // when
            repo.delete(entityToBeDeleted)

            // then
            val deletedResult = databaseOps.findEntityById(idToBeDeleted)
            deletedResult shouldBe null

            val actualResult = databaseOps.findEntityById(id)
            actualResult shouldBe entityToStay
        }
    }

    body(repo, entityOps, databaseOps, this)
})