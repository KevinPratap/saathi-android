package com.saathi.engine

import com.saathi.model.ScamCategory
import java.util.ArrayDeque

/**
 * Result representation of a keyword pattern match in text.
 */
data class TrieMatch(
    val keyword: String,
    val category: ScamCategory,
    val ruleId: String,
    val startIndex: Int,
    val endIndex: Int
)

/**
 * Output data associated with a match state in the Aho-Corasick automaton.
 */
data class TrieMatchOutput(
    val keyword: String,
    val category: ScamCategory,
    val ruleId: String
)

/**
 * Trie node structure supporting Aho-Corasick failure transitions.
 */
class TrieNode {
    val children = HashMap<Char, TrieNode>()
    var isEndOfWord: Boolean = false
    var keyword: String? = null
    var category: ScamCategory? = null
    var ruleId: String? = null
    var failNode: TrieNode? = null
    val outputs = mutableListOf<TrieMatchOutput>()
}

/**
 * High-performance Aho-Corasick multi-pattern search trie.
 * Enables simultaneous O(N) substring scanning across large keyword dictionaries.
 */
class Trie {
    val root = TrieNode()
    private var isBuilt = false

    /**
     * Inserts a keyword pattern into the trie.
     */
    fun insert(keyword: String, category: ScamCategory, ruleId: String) {
        if (keyword.isBlank()) return
        val normalized = keyword.lowercase()
        var current = root
        for (char in normalized) {
            current = current.children.computeIfAbsent(char) { TrieNode() }
        }
        current.isEndOfWord = true
        current.keyword = normalized
        current.category = category
        current.ruleId = ruleId
        // Add direct output if not already present
        if (current.outputs.none { it.keyword == normalized && it.ruleId == ruleId }) {
            current.outputs.add(TrieMatchOutput(normalized, category, ruleId))
        }
        isBuilt = false
    }

    /**
     * Builds Aho-Corasick failure and dictionary transition pointers via BFS.
     */
    fun buildFailureTransitions() {
        val queue = ArrayDeque<TrieNode>()
        for (child in root.children.values) {
            child.failNode = root
            queue.add(child)
        }

        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: continue
            for ((char, child) in current.children) {
                var fail = current.failNode
                while (fail != null && !fail.children.containsKey(char)) {
                    fail = fail.failNode
                }
                val targetFail = fail?.children?.get(char) ?: root
                child.failNode = targetFail
                // Inherit outputs from fail node
                for (out in targetFail.outputs) {
                    if (child.outputs.none { it.keyword == out.keyword && it.ruleId == out.ruleId }) {
                        child.outputs.add(out)
                    }
                }
                queue.add(child)
            }
        }
        isBuilt = true
    }

    /**
     * Searches the given text for all matching dictionary keywords in O(N) time.
     */
    fun search(text: String): List<TrieMatch> {
        if (!isBuilt) {
            buildFailureTransitions()
        }
        val matches = mutableListOf<TrieMatch>()
        if (text.isEmpty()) return matches

        val normalizedText = text.lowercase()
        var current = root

        for (i in normalizedText.indices) {
            val char = normalizedText[i]
            while (current != root && !current.children.containsKey(char)) {
                current = current.failNode ?: root
            }
            current = current.children[char] ?: root

            for (output in current.outputs) {
                val start = i - output.keyword.length + 1
                val end = i + 1
                if (start >= 0) {
                    matches.add(TrieMatch(output.keyword, output.category, output.ruleId, start, end))
                }
            }
        }
        return matches
    }

    /**
     * Exact dictionary match check for a full word or phrase.
     */
    fun exactMatch(word: String): TrieMatch? {
        if (word.isBlank()) return null
        val normalized = word.lowercase()
        var current = root
        for (char in normalized) {
            current = current.children[char] ?: return null
        }
        return if (current.isEndOfWord && current.keyword != null && current.category != null && current.ruleId != null) {
            TrieMatch(current.keyword!!, current.category!!, current.ruleId!!, 0, normalized.length)
        } else {
            null
        }
    }

    /**
     * Returns true if any dictionary entry begins with the given prefix.
     */
    fun startsWith(prefix: String): Boolean {
        if (prefix.isBlank()) return false
        val normalized = prefix.lowercase()
        var current = root
        for (char in normalized) {
            current = current.children[char] ?: return false
        }
        return true
    }

    /**
     * Resets the trie, clearing all nodes and compiled transitions.
     */
    fun clear() {
        root.children.clear()
        root.outputs.clear()
        root.isEndOfWord = false
        root.keyword = null
        root.category = null
        root.ruleId = null
        root.failNode = null
        isBuilt = false
    }
}
