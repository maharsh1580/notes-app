package com.example.notes.ui

import com.example.notes.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    private class FakeApi : NotesApi {
        var failSave = false
        var createCount = 0
        val note = Note("one", "Title", "Body", "now", "now")
        override suspend fun list(limit: Int, offset: Int) = emptyList<Note>()
        override suspend fun get(id: String) = note
        override suspend fun create(note: NoteInput): Note {
            createCount++
            if (failSave) throw IOException("offline")
            return this.note.copy(title = note.title, content = note.content)
        }
        override suspend fun update(id: String, note: NotePatch) = this.note
        override suspend fun delete(id: String) = Unit
    }

    @Test fun failedSavePreservesDraft() = runTest(dispatcher) {
        val api = FakeApi().apply { failSave = true }
        val model = NotesViewModel(NotesRepository(api))
        advanceUntilIdle()
        model.newNote()
        model.titleChanged("Unsaved")
        model.contentChanged("Keep this text")
        model.save()
        advanceUntilIdle()
        assertTrue(model.state.value.editing)
        assertEquals("Keep this text", model.state.value.content)
        assertNotNull(model.state.value.error)
        assertFalse(model.state.value.busy)
        api.failSave = false
        model.save()
        advanceUntilIdle()
        assertFalse(model.state.value.editing)
        assertEquals("Unsaved", model.state.value.notes.single().title)
    }

    @Test fun repeatedSaveOnlyCreatesOnce() = runTest(dispatcher) {
        val api = FakeApi()
        val model = NotesViewModel(NotesRepository(api))
        advanceUntilIdle()
        model.newNote()
        model.titleChanged("New")
        model.save()
        model.save()
        advanceUntilIdle()
        assertEquals(1, api.createCount)
    }

    @Test fun invalidTitleDoesNotCallServer() = runTest(dispatcher) {
        val api = FakeApi()
        val model = NotesViewModel(NotesRepository(api))
        advanceUntilIdle()
        model.newNote()
        model.titleChanged("  ")
        model.save()
        advanceUntilIdle()
        assertEquals(0, api.createCount)
        assertNotNull(model.state.value.error)
    }
}
