package com.orgzly.android.ui.note.links

import com.orgzly.android.ui.views.style.IdLinkSpan
import java.util.Locale

/** A note that can be selected as the target of an org [id:] link. */
data class LinkTarget(
    val noteId: Long,
    val title: String,
    /** Notebook followed by the heading's ancestors, but not the heading itself. */
    val context: String
)

/**
 * Keeps the picker search independent of Android views and the database.
 *
 * Matching is a case-insensitive subsequence match. A contiguous or prefix match is ranked
 * ahead of a match with gaps, and title matches are always ranked ahead of context-only ones.
 * Stable alphabetical/id tie-breakers make the ordering predictable between searches.
 */
object LinkTargetSearch {
    fun filter(targets: Collection<LinkTarget>, query: String): List<LinkTarget> {
        val normalizedQuery = searchKey(query)

        if (normalizedQuery.isEmpty()) {
            return targets.sortedWith(defaultOrder)
        }

        return targets.mapNotNull { target ->
            val titleScore = subsequenceScore(normalizedQuery, searchKey(target.title))
            val contextScore = subsequenceScore(
                normalizedQuery,
                searchKey("${target.title} ${target.context}")
            )

            when {
                titleScore != null -> RankedTarget(target, matchedTitle = true, titleScore)
                contextScore != null -> RankedTarget(target, matchedTitle = false, contextScore)
                else -> null
            }
        }.sortedWith(
            compareByDescending<RankedTarget> { it.matchedTitle }
                .thenByDescending { it.score }
                .thenBy { searchKey(it.target.title) }
                .thenBy { searchKey(it.target.context) }
                .thenBy { it.target.noteId }
        ).map { it.target }
    }

    private data class RankedTarget(
        val target: LinkTarget,
        val matchedTitle: Boolean,
        val score: Int
    )

    private val defaultOrder = compareBy<LinkTarget> { searchKey(it.title) }
        .thenBy { searchKey(it.context) }
        .thenBy { it.noteId }

    private fun searchKey(value: String): String =
        value.lowercase(Locale.ROOT).filter(Char::isLetterOrDigit)

    /**
     * Score a subsequence, rewarding exact, prefix and contiguous matches while penalising gaps
     * and late starts. Null means that not every query character can be matched in order.
     */
    private fun subsequenceScore(query: String, candidate: String): Int? {
        if (query.isEmpty()) return 0

        var queryIndex = 0
        var firstMatch = -1
        var previousMatch = -2
        var gaps = 0
        var contiguousPairs = 0

        candidate.forEachIndexed { index, character ->
            if (queryIndex < query.length && character == query[queryIndex]) {
                if (firstMatch == -1) firstMatch = index
                if (index == previousMatch + 1) {
                    contiguousPairs++
                } else if (previousMatch >= 0) {
                    gaps += index - previousMatch - 1
                }
                previousMatch = index
                queryIndex++
            }
        }

        if (queryIndex != query.length) return null

        val exact = candidate == query
        val prefix = candidate.startsWith(query)

        return (if (exact) 1_000_000 else 0) +
            (if (prefix) 100_000 else 0) +
            contiguousPairs * 1_000 -
            gaps * 20 -
            firstMatch * 2 -
            candidate.length
    }
}

object IdLinkFormatter {
    fun format(id: String, title: String): String =
        "[[${IdLinkSpan.PREFIX}$id][$title]]"
}
