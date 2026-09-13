package com.example.notes.data

class NotesRepository(private val api: NotesApi) {
    suspend fun list(offset: Int) = api.list(offset = offset)
    suspend fun get(id: String) = api.get(id)
    suspend fun save(id: String?, title: String, content: String): Note =
        if (id == null) api.create(NoteInput(title.trim(), content))
        else api.update(id, NotePatch(title.trim(), content))
    suspend fun delete(id: String) = api.delete(id)
}
