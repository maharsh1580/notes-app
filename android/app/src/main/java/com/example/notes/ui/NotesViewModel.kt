package com.example.notes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notes.BuildConfig
import com.example.notes.data.ApiClient
import com.example.notes.data.Note
import com.example.notes.data.NotesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesState(
    val notes: List<Note> = emptyList(),
    val busy: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = false,
    val editing: Boolean = false,
    val editId: String? = null,
    val title: String = "",
    val content: String = "",
)

class NotesViewModel(
    private val repository: NotesRepository
) : ViewModel() {

    private val mutableState = MutableStateFlow(NotesState())
    val state = mutableState.asStateFlow()

    init {
        refresh()
    }

    private fun launchRequest(action: suspend () -> Unit) {
        if (state.value.busy) return

        mutableState.update {
            it.copy(
                busy = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {

                e.printStackTrace()

                // TEMPORARY DEBUG MESSAGE
                // Shows the real exception directly inside the app.
                val message = """
                    ERROR: ${e.javaClass.name}

                    MESSAGE: ${e.message ?: "No message"}

                    API URL: ${BuildConfig.API_BASE_URL}
                """.trimIndent()

                mutableState.update {
                    it.copy(error = message)
                }

            } finally {
                mutableState.update {
                    it.copy(busy = false)
                }
            }
        }
    }

    fun refresh() = launchRequest {
        val notes = repository.list(0)

        mutableState.update {
            it.copy(
                notes = notes,
                hasMore = notes.size == 100
            )
        }
    }

    fun loadMore() = launchRequest {
        val notes = repository.list(state.value.notes.size)

        mutableState.update {
            it.copy(
                notes = (it.notes + notes).distinctBy(Note::id),
                hasMore = notes.size == 100
            )
        }
    }

    fun newNote() {
        if (state.value.busy) return

        mutableState.update {
            it.copy(
                editing = true,
                editId = null,
                title = "",
                content = "",
                error = null
            )
        }
    }

    fun edit(note: Note) = launchRequest {
        val current = repository.get(note.id)

        mutableState.update {
            it.copy(
                editing = true,
                editId = current.id,
                title = current.title,
                content = current.content
            )
        }
    }

    fun titleChanged(value: String) {
        mutableState.update {
            it.copy(title = value)
        }
    }

    fun contentChanged(value: String) {
        mutableState.update {
            it.copy(content = value)
        }
    }

    fun closeEditor() {
        if (!state.value.busy) {
            mutableState.update {
                it.copy(
                    editing = false,
                    error = null
                )
            }
        }
    }

    fun save() {
        val draft = state.value

        if (
            draft.title.trim().isEmpty() ||
            draft.title.trim().length > 200 ||
            draft.content.length > 10000
        ) {
            mutableState.update {
                it.copy(
                    error = "Use a title of 1–200 characters and a note up to 10,000 characters."
                )
            }
            return
        }

        launchRequest {
            val note = repository.save(
                draft.editId,
                draft.title,
                draft.content
            )

            mutableState.update {

                val notes =
                    if (draft.editId == null) {
                        listOf(note) + it.notes
                    } else {
                        it.notes.map { old ->
                            if (old.id == note.id) note else old
                        }
                    }

                it.copy(
                    notes = notes,
                    editing = false
                )
            }
        }
    }

    fun delete(note: Note) = launchRequest {
        repository.delete(note.id)

        mutableState.update {
            it.copy(
                notes = it.notes.filterNot { old ->
                    old.id == note.id
                }
            )
        }
    }

    companion object {

        val Factory = object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {

                require(
                    modelClass.isAssignableFrom(
                        NotesViewModel::class.java
                    )
                )

                return NotesViewModel(
                    NotesRepository(
                        ApiClient.create(
                            BuildConfig.API_BASE_URL
                        )
                    )
                ) as T
            }
        }
    }
}