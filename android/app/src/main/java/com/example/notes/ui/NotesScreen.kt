package com.example.notes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notes.data.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(model: NotesViewModel) {
    val state by model.state.collectAsStateWithLifecycle()
    var deleting by remember { mutableStateOf<Note?>(null) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Notes") }, actions = {
            TextButton(onClick = model::refresh, enabled = !state.busy) { Text("Refresh") }
        }) },
        floatingActionButton = {
            if (!state.editing) FloatingActionButton(onClick = model::newNote) { Text("+ New", Modifier.padding(16.dp)) }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (!state.editing) state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.notes.isEmpty() && !state.busy) {
                Text("A little space for your thoughts", style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 32.dp))
                Text("Tap + New to write your first note.", Modifier.padding(top = 8.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)) {
                items(state.notes, key = { it.id }) { note ->
                    Card(onClick = { model.edit(note) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(note.title, style = MaterialTheme.typography.titleLarge, maxLines = 2,
                                overflow = TextOverflow.Ellipsis)
                            if (note.content.isNotEmpty()) Text(note.content, maxLines = 4,
                                overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
                            Row {
                                TextButton(onClick = { model.edit(note) }, enabled = !state.busy) { Text("Open") }
                                TextButton(onClick = { deleting = note }, enabled = !state.busy) { Text("Delete") }
                            }
                        }
                    }
                }
                if (state.hasMore) item {
                    TextButton(onClick = model::loadMore, enabled = !state.busy) { Text("Load more") }
                }
            }
        }
    }
    if (state.editing) AlertDialog(
        onDismissRequest = model::closeEditor,
        title = { Text(if (state.editId == null) "New note" else "Edit note") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = state.title, onValueChange = model::titleChanged,
                    label = { Text("Title") }, singleLine = true, enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.content, onValueChange = model::contentChanged,
                    label = { Text("Note") }, minLines = 5, maxLines = 12, enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = model::save, enabled = !state.busy) { Text("Save") } },
        dismissButton = { TextButton(onClick = model::closeEditor, enabled = !state.busy) { Text("Cancel") } },
    )
    deleting?.let { note ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Delete note?") },
            text = { Text("Delete “${note.title}”? This cannot be undone.") },
            confirmButton = { TextButton(onClick = { deleting = null; model.delete(note) },
                enabled = !state.busy) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } })
    }
}
