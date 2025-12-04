package fm.apakabar.syllabreak

class WordSyllabifier(private val rule: LanguageRule) {
    fun syllabifyWord(
        word: String,
        softHyphen: String,
    ): String {
        val syllabifier = WordSyllabification(word, rule, softHyphen)
        return syllabifier.syllabify()
    }
}

private class WordSyllabification(
    private val word: String,
    private val rule: LanguageRule,
    private val softHyphen: String,
) {
    private val tokens: List<SyllableToken> = tokenize()
    private val nuclei: List<Int> = findNuclei()

    private fun tokenize(): List<SyllableToken> {
        val tokenizer = SyllableTokenizer(word, rule)
        return tokenizer.tokenize()
    }

    private fun findNuclei(): List<Int> {
        val nuclei = mutableListOf<Int>()

        // First pass: find vowels
        tokens.forEachIndexed { index, token ->
            if (token.tokenClass == TokenClass.VOWEL) {
                nuclei.add(index)
            }
        }

        // If no vowels found, look for syllabic consonants
        if (nuclei.isEmpty()) {
            tokens.forEachIndexed { index, token ->
                if (token.tokenClass == TokenClass.CONSONANT &&
                    token.surface.lowercase().firstOrNull() in rule.syllabicConsonants
                ) {
                    nuclei.add(index)
                }
            }
        }

        return nuclei
    }

    private fun skipSeparatorsForward(start: Int): Int {
        var pos = start
        while (pos < tokens.size && tokens[pos].tokenClass == TokenClass.SEPARATOR) {
            pos++
        }
        return pos
    }

    private fun skipSeparatorsBackward(start: Int): Int {
        var pos = start
        while (pos >= 0 && tokens[pos].tokenClass == TokenClass.SEPARATOR) {
            pos--
        }
        return pos
    }

    private fun extractConsonantCluster(
        left: Int,
        right: Int,
    ): Pair<List<SyllableToken>, List<Int>> {
        val cluster = mutableListOf<SyllableToken>()
        val clusterIndices = mutableListOf<Int>()

        for (i in left..right) {
            if (i < tokens.size && tokens[i].tokenClass == TokenClass.CONSONANT) {
                cluster.add(tokens[i])
                clusterIndices.add(i)
            }
        }

        return cluster to clusterIndices
    }

    private fun findClusterBetweenNuclei(
        nk: Int,
        nk1: Int,
    ): Pair<List<SyllableToken>, List<Int>> {
        val left = skipSeparatorsForward(nk + 1)
        val right = skipSeparatorsBackward(nk1 - 1)
        return extractConsonantCluster(left, right)
    }

    private fun isValidOnset(
        consonant1: String,
        consonant2: String,
        prevNucleusIdx: Int? = null,
    ): Boolean {
        val onsetCandidate = consonant1.lowercase() + consonant2.lowercase()

        // Check if this cluster requires a long vowel before it
        if (onsetCandidate in rule.clustersOnlyAfterLong && prevNucleusIdx != null) {
            if (!isLongNucleus(prevNucleusIdx)) {
                return false
            }
        }

        return onsetCandidate in rule.clustersKeepNext
    }

    private fun isLongNucleus(nucleusIdx: Int): Boolean {
        if (nucleusIdx >= tokens.size) return false

        val vowelToken = tokens[nucleusIdx]

        // Check if this vowel token itself is already a digraph
        if (vowelToken.surface.lowercase() in rule.digraphVowels) {
            return true
        }

        // Check if current vowel + next character forms a digraph vowel
        if (nucleusIdx + 1 < tokens.size) {
            val nextToken = tokens[nucleusIdx + 1]
            val digraph = vowelToken.surface.lowercase() + nextToken.surface.lowercase()
            if (digraph in rule.digraphVowels) {
                return true
            }
        }

        return false
    }

    private fun findBoundaryInCluster(
        cluster: List<SyllableToken>,
        clusterIndices: List<Int>,
        nk: Int,
        nk1: Int,
    ): Int? {
        return when (cluster.size) {
            0 -> {
                // Check for vowel hiatus (adjacent vowels that form separate syllables)
                if (!rule.splitHiatus) {
                    return null
                }

                // Check if nuclei are adjacent (or only separated by modifiers/separators)
                val areAdjacent =
                    if (nk1 - nk == 1) {
                        true
                    } else {
                        (nk + 1 until nk1).all {
                            tokens[it].tokenClass == TokenClass.SEPARATOR
                        }
                    }

                if (areAdjacent) {
                    // Check if these two vowels form a digraph (don't split)
                    val vowelPair = tokens[nk].surface.lowercase() + tokens[nk1].surface.lowercase()
                    if (vowelPair in rule.digraphVowels) {
                        return null
                    }
                    // Hiatus: split between vowels
                    return nk1
                }
                null
            }
            1 -> clusterIndices[0] // V-CV: boundary before single consonant
            2 -> {
                // Two consonant cluster
                if (isValidOnset(cluster[0].surface, cluster[1].surface, nk)) {
                    clusterIndices[0]
                } else {
                    clusterIndices[1]
                }
            }
            else -> {
                // Long cluster (3+ consonants)
                var boundaryIdx = clusterIndices.last()
                if (cluster.size >= 2 &&
                    isValidOnset(cluster[cluster.size - 2].surface, cluster.last().surface, nk)
                ) {
                    boundaryIdx = clusterIndices[clusterIndices.size - 2]
                }
                boundaryIdx
            }
        }
    }

    private fun placeBoundaries(): List<Int> {
        val boundaries = mutableListOf<Int>()

        for (k in 0 until nuclei.size - 1) {
            val nk = nuclei[k]
            val nk1 = nuclei[k + 1]

            val (cluster, clusterIndices) = findClusterBetweenNuclei(nk, nk1)
            val boundaryIdx = findBoundaryInCluster(cluster, clusterIndices, nk, nk1)

            if (boundaryIdx != null) {
                boundaries.add(boundaryIdx)
            }
        }

        return boundaries
    }

    fun syllabify(): String {
        if (nuclei.isEmpty()) {
            return word
        }

        val boundaries = placeBoundaries()
        if (boundaries.isEmpty()) {
            return word
        }

        val result = StringBuilder()
        var lastBoundary = 0

        for (boundary in boundaries) {
            // Add syllable up to boundary
            for (i in lastBoundary until boundary) {
                if (i < tokens.size) {
                    result.append(tokens[i].surface)
                }
            }
            result.append(softHyphen)
            lastBoundary = boundary
        }

        // Add remaining tokens
        for (i in lastBoundary until tokens.size) {
            result.append(tokens[i].surface)
        }

        return result.toString()
    }
}
