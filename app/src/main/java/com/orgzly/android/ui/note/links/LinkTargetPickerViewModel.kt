package com.orgzly.android.ui.note.links

import androidx.lifecycle.MutableLiveData
import com.orgzly.android.App
import com.orgzly.android.data.DataRepository
import com.orgzly.android.db.entity.NoteView
import com.orgzly.android.ui.CommonViewModel
import com.orgzly.android.ui.SingleLiveEvent

class LinkTargetPickerViewModel(
    private val dataRepository: DataRepository
) : CommonViewModel() {

    data class Selection(val noteId: Long, val title: String, val id: String)

    val targets = MutableLiveData<List<LinkTarget>>()
    val selectedEvent = SingleLiveEvent<Selection>()

    private var allTargets: List<LinkTarget> = emptyList()
    private var query = ""
    private var loaded = false

    val currentQuery: String
        get() = query

    fun load() {
        if (loaded) return
        loaded = true

        App.EXECUTORS.diskIO().execute {
            catchAndPostError {
                allTargets = buildTargets(dataRepository.getAllLinkableNoteViews())
                targets.postValue(LinkTargetSearch.filter(allTargets, query))
            }
        }
    }

    fun setQuery(query: String) {
        this.query = query
        if (loaded && allTargets.isNotEmpty()) {
            targets.value = LinkTargetSearch.filter(allTargets, query)
        }
    }

    fun select(target: LinkTarget) {
        App.EXECUTORS.diskIO().execute {
            catchAndPostError {
                val id = dataRepository.getOrCreateNoteId(target.noteId) ?: return@catchAndPostError
                selectedEvent.postValue(Selection(target.noteId, target.title, id))
            }
        }
    }

    private fun buildTargets(noteViews: List<NoteView>): List<LinkTarget> {
        val orderedNotes = noteViews.sortedWith(
            compareBy<NoteView> { it.note.position.bookId }
                .thenBy { it.note.position.lft }
                .thenBy { it.note.id }
        )
        val ancestors = mutableListOf<NoteView>()
        var bookId: Long? = null

        return orderedNotes.map { noteView ->
            if (bookId != noteView.note.position.bookId) {
                bookId = noteView.note.position.bookId
                ancestors.clear()
            }

            while ((ancestors.lastOrNull()?.note?.position?.level ?: -1) >= noteView.note.position.level) {
                ancestors.removeAt(ancestors.lastIndex)
            }

            val context = buildList {
                add(noteView.bookName)
                addAll(ancestors.map { it.note.title }.filter { it.isNotBlank() })
            }.joinToString(CONTEXT_SEPARATOR)

            ancestors += noteView
            LinkTarget(noteView.note.id, noteView.note.title, context)
        }
    }

    companion object {
        const val CONTEXT_SEPARATOR = " \u203a "
    }
}
