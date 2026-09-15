package com.orgzly.android.ui.note.links

import org.junit.Assert.assertEquals
import org.junit.Test

class LinkTargetPickerTest {

    @Test
    fun fuzzySearchRanksPrefixMatchesAheadOfSparseSubsequences() {
        val prefix = LinkTarget(1, "Project notes", "Work")
        val sparse = LinkTarget(2, "Paper road", "Home")

        val results = LinkTargetSearch.filter(listOf(sparse, prefix), "pro")

        assertEquals(listOf(prefix, sparse), results)
    }

    @Test
    fun fuzzySearchIsCaseInsensitiveAndIgnoresSeparators() {
        val matching = LinkTarget(1, "Project notes", "Work")
        val notMatching = LinkTarget(2, "Roadmap", "Work")

        val results = LinkTargetSearch.filter(listOf(notMatching, matching), "P-N")

        assertEquals(listOf(matching), results)
    }

    @Test
    fun fuzzySearchIncludesContextButRanksTitleMatchesFirstAndBreaksTiesDeterministically() {
        val titleMatch = LinkTarget(3, "Planning", "Personal")
        val contextMatchB = LinkTarget(2, "Inbox", "Work › Planning")
        val contextMatchA = LinkTarget(1, "Inbox", "Home › Planning")

        val results = LinkTargetSearch.filter(
            listOf(contextMatchB, titleMatch, contextMatchA),
            "plan"
        )

        assertEquals(listOf(titleMatch, contextMatchA, contextMatchB), results)
    }

    @Test
    fun linkFormatterUsesIdLinkSyntax() {
        assertEquals(
            "[[id:existing-id][Target title]]",
            IdLinkFormatter.format("existing-id", "Target title")
        )
    }
}
