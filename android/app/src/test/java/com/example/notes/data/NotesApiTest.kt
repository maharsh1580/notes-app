package com.example.notes.data

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class NotesApiTest {
    private val server = MockWebServer()
    private lateinit var api: NotesApi
    private val json = """{"id":"abc","title":"Title","content":"Body","created_at":"2026-09-13T00:00:00Z","updated_at":"2026-09-13T00:00:00Z"}"""
    @Before fun setup() { server.start(); api = ApiClient.create(server.url("/").toString()) }
    @After fun teardown() { server.shutdown() }
    private fun enqueue(body: String, status: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body))
    }

    @Test fun crudWireContract() = runTest {
        enqueue("[$json]")
        assertEquals("2026-09-13T00:00:00Z", api.list(offset = 100).single().createdAt)
        assertEquals("/api/v1/notes?limit=100&offset=100", server.takeRequest(1, TimeUnit.SECONDS)!!.path)
        enqueue(json, 201)
        assertEquals("abc", api.create(NoteInput("Title", "Body")).id)
        val post = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("POST", post.method)
        assertTrue(post.body.readUtf8().contains("\"content\":\"Body\""))
        enqueue(json)
        api.get("abc")
        assertEquals("/api/v1/notes/abc", server.takeRequest(1, TimeUnit.SECONDS)!!.path)
        enqueue(json)
        api.update("abc", NotePatch(content = ""))
        val patch = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("PATCH", patch.method)
        assertEquals("{\"content\":\"\"}", patch.body.readUtf8())
        server.enqueue(MockResponse().setResponseCode(204))
        api.delete("abc")
        assertEquals("DELETE", server.takeRequest(1, TimeUnit.SECONDS)!!.method)
    }

    @Test fun errorsPropagate() = runTest {
        enqueue("{\"detail\":\"Note not found\"}", 404)
        try { api.get("missing"); fail("Expected HTTP error") }
        catch (e: HttpException) { assertEquals(404, e.code()) }
    }
}
