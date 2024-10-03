package bg.dalexiev.todo.user

import bg.dalexiev.todo.utils.CrudRepositorySpec
import bg.dalexiev.todo.utils.DatabaseOps
import bg.dalexiev.todo.utils.EntityOps
import bg.dalexiev.todo.utils.JdbcTemplate
import io.kotest.matchers.shouldBe
import kotlin.random.Random

object UserEntityOps : EntityOps<RegisteredUser, Long> {

    override fun createNewEntity(id: Long): RegisteredUser =
        RegisteredUser(
            id = id,
            email = EmailAddress("test_$id@test.com"),
            password = HashedPassword("hash")
        )

    override fun generateNewId(): Long = Random.Default.nextLong(1000L, 30000L)

}

class UserDatabaseOps(private val jdbcTemplate: JdbcTemplate) : DatabaseOps<RegisteredUser, Long> {

    override fun findEntityById(id: Long): RegisteredUser? =
        jdbcTemplate.queryForObject("select id, email, password from users where id = ?", listOf(id)) {
            RegisteredUser(
                id = it.getLong("id"),
                email = EmailAddress(it.getString("email")),
                password = HashedPassword(it.getString("password"))
            )
        }

    override fun insertEntity(entity: RegisteredUser): Long =
        jdbcTemplate.insert(
            "insert into users values(?, ?, ?)",
            listOf(entity.id, entity.email.value, entity.password.value)
        ) {
            it.getLong(1)
        } ?: throw AssertionError("Could not insert user")

    fun countUsers(): Int? =
        jdbcTemplate.queryForObject("select count(id) from users") {
            it.getInt(1)
        }

}

class UserPersistenceTest : CrudRepositorySpec<RegisteredUser, Long, UserRepository, UserDatabaseOps>(
    repo = UserRepository(),
    entityOps = UserEntityOps,
    databaseOpsFactory = { UserDatabaseOps(it) },
    body = {
        val email = "test@test.com"
        val password = "Secret!23"

        val expectedUser = RegisteredUser(
            id = 1L,
            email = EmailAddress(email),
            password = HashedPassword(password)
        )

        context("create") {
            test("inserts RegisteredUser") {
                // when
                val actualResult = create(email, password)

                // then
                actualResult shouldBe expectedUser

                val persistedUser = findEntityById(1L)
                persistedUser shouldBe expectedUser
            }

            test("returns null when creating user with duplicate email") {
                // given
                insertEntity(expectedUser)

                // when
                val actualResult = create(email, password)

                // then
                actualResult shouldBe null

                val userCount = countUsers()
                userCount shouldBe 1
            }
        }

        context("findByEmail")
        {

            beforeTest { 
                insertEntity(expectedUser)
            }

            test("returns RegisteredUser") {
                // when
                val actualResult = findByEmail(email)

                // then
                actualResult shouldBe expectedUser
            }

            test("returns null when no user with provided email exists") {
                // when
                val actualResult = findByEmail("unknown@test.com")

                // then
                actualResult shouldBe null
            }
        }

    }
)