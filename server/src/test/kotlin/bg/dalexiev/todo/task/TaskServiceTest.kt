package bg.dalexiev.todo.task

import bg.dalexiev.todo.utils.fixed
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.datetime.Clock

class TaskServiceTest : FunSpec({

    val repo = mockk<TaskRepository>()
    val clock = Clock.fixed(fixedAt = Clock.System.now())

    val service = taskService(repo, clock)

    val userId = 1L

    afterTest { 
        clearAllMocks()
    }
    
    context("getTasks") {
        test("returns empty list") {
            coEvery { repo.findByUserId(any()) } returns emptyList()

            val actualResult = service.getTasks(userId)

            actualResult shouldBe emptyList()

            coVerify { repo.findByUserId(userId) }
        }

        test("returns tasks") {
            val expectedTasks = listOf(
                Task(
                    id = 1L,
                    title = "title",
                    description = "description",
                    completed = false,
                    createdAt = Clock.System.now(),
                    userId = userId
                )
            )
            coEvery { repo.findByUserId(any()) } returns expectedTasks

            val actualResult = service.getTasks(userId)

            actualResult shouldBe expectedTasks

            coVerify { repo.findByUserId(userId) }
        }
    }

    context("createTask") {
        test("creates a new task") {
            val title = "title"
            val description = "description"
            val createdAt = clock.now()

            val expectedTask = Task(
                id = 1L,
                title = title,
                description = description,
                completed = false,
                createdAt = createdAt,
                userId = userId
            )

            coEvery { repo.create(any(), any(), any(), any(), any()) } returns expectedTask

            val actualResult = service.createTask(userId, title, description)

            actualResult shouldBe expectedTask

            coVerify { repo.create(userId, title, description, false, createdAt) }
        }
    }

    context("updateTask") {
        val id = 1L

        test("updates an existing task") {
            val task = Task(
                id = id,
                title = "title",
                description = "description",
                completed = false,
                createdAt = clock.now(),
                userId = userId
            )

            val expectedTask = task.copy(completed = true)

            coEvery { repo.findById(any()) } returns task
            coEvery { repo.save(any()) } returnsArgument 0

            val actualResult = service.updateTask(id, true)

            actualResult shouldBe expectedTask

            coVerifyOrder {
                repo.findById(id)
                repo.save(expectedTask)
            }
        }

        test("returns null if no task found") {
            coEvery { repo.findById(any()) } returns null

            val actualResult = service.updateTask(id, true)

            actualResult shouldBe null

            coVerify { repo.findById(id) }
            coVerify(exactly = 0) { repo.save(any()) }
        }
    }

    context("deleteTask") {
        test("return true when a task is deleted") {
            val id = 1L
            
            coEvery { repo.deleteById(any()) } returns 1

            val actualResult = service.deleteTask(id)

            actualResult shouldBe true
            
            coVerify { repo.deleteById(id) }
        }
        
        test("returns false if a task is not deleted") {
            val id = 1L
            
            coEvery { repo.deleteById(any()) } returns 0
            
            val actualResult = service.deleteTask(id)
            
            actualResult shouldBe false
            
            coVerify { repo.deleteById(id) }
        }
    }
})