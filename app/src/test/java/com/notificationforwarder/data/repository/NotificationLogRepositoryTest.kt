package com.notificationforwarder.data.repository

import com.notificationforwarder.data.dao.NotificationLogDao
import com.notificationforwarder.data.entity.NotificationLog
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NotificationLogRepositoryTest {

    private lateinit var repository: NotificationLogRepository
    private val dao: NotificationLogDao = mockk()

    @Before
    fun setup() {
        repository = NotificationLogRepository(dao)
    }

    @Test
    fun `getAllLogs returns flow from dao`() = runTest {
        val logs = listOf(
            createTestLog(id = 1),
            createTestLog(id = 2)
        )
        every { dao.getAllFlow() } returns flowOf(logs)

        val result = repository.getAllLogs().first()

        assertThat(result).hasSize(2)
        verify { dao.getAllFlow() }
    }

    @Test
    fun `getLogsByStatus returns filtered logs`() = runTest {
        val successLogs = listOf(createTestLog(success = true))
        every { dao.getByStatusFlow(true) } returns flowOf(successLogs)

        val result = repository.getLogsByStatus(true).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].success).isTrue()
        verify { dao.getByStatusFlow(true) }
    }

    @Test
    fun `insertLog calls dao insert`() = runTest {
        val log = createTestLog()
        coEvery { dao.insert(log) } returns 1L
        coEvery { dao.getCount() } returns 500

        val id = repository.insertLog(log)

        assertThat(id).isEqualTo(1L)
        coVerify { dao.insert(log) }
        coVerify { dao.getCount() }
    }

    @Test
    fun `insertLog triggers pruning when count exceeds 1000`() = runTest {
        val log = createTestLog()
        coEvery { dao.insert(log) } returns 1L
        coEvery { dao.getCount() } returns 1001
        coEvery { dao.pruneOldLogs() } just Runs

        repository.insertLog(log)

        coVerify { dao.pruneOldLogs() }
    }

    @Test
    fun `insertLog does not trigger pruning when count is below 1000`() = runTest {
        val log = createTestLog()
        coEvery { dao.insert(log) } returns 1L
        coEvery { dao.getCount() } returns 999

        repository.insertLog(log)

        coVerify(exactly = 0) { dao.pruneOldLogs() }
    }

    @Test
    fun `clearAllLogs calls dao deleteAll`() = runTest {
        coEvery { dao.deleteAll() } just Runs

        repository.clearAllLogs()

        coVerify { dao.deleteAll() }
    }

    @Test
    fun `deleteLogsOlderThan calls dao with timestamp`() = runTest {
        val timestamp = 1234567890L
        coEvery { dao.deleteOlderThan(timestamp) } just Runs

        repository.deleteLogsOlderThan(timestamp)

        coVerify { dao.deleteOlderThan(timestamp) }
    }

    private fun createTestLog(
        id: Long = 1L,
        webhookId: Long = 1L,
        webhookUrl: String = "https://example.com",
        packageName: String = "com.test",
        appName: String = "Test",
        title: String = "Title",
        text: String = "Text",
        priority: Int = 0,
        timestamp: Long = System.currentTimeMillis(),
        success: Boolean = true
    ) = NotificationLog(
        id = id,
        webhookId = webhookId,
        webhookUrl = webhookUrl,
        packageName = packageName,
        appName = appName,
        title = title,
        text = text,
        priority = priority,
        timestamp = timestamp,
        success = success
    )
}

