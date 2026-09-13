package com.example.notes.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class Note(
    val id: String,
    val title: String,
    val content: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
)

data class NoteInput(val title: String, val content: String)
// Gson omits nulls, matching the API's absent-field PATCH semantics.
data class NotePatch(val title: String? = null, val content: String? = null)

interface NotesApi {
    @GET("api/v1/notes")
    suspend fun list(@Query("limit") limit: Int = 100, @Query("offset") offset: Int = 0): List<Note>

    @GET("api/v1/notes/{id}")
    suspend fun get(@Path("id") id: String): Note

    @POST("api/v1/notes")
    suspend fun create(@Body note: NoteInput): Note

    @PATCH("api/v1/notes/{id}")
    suspend fun update(@Path("id") id: String, @Body note: NotePatch): Note

    @DELETE("api/v1/notes/{id}")
    suspend fun delete(@Path("id") id: String)
}
