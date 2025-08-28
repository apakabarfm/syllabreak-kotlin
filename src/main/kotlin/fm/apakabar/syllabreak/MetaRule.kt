package fm.apakabar.syllabreak

class MetaRule(val rules: List<LanguageRule>) {
    init {
        calculateUniqueChars()
        linkRulesToMeta()
    }

    private fun calculateUniqueChars() {
        for (rule in rules) {
            rule.uniqueChars =
                rule.allChars.toMutableSet().apply {
                    for (otherRule in rules) {
                        if (otherRule.lang != rule.lang) {
                            removeAll(otherRule.allChars)
                        }
                    }
                }
        }
    }

    private fun linkRulesToMeta() {
        for (rule in rules) {
            rule.meta = this
        }
    }

    fun getAllKnownChars(): Set<Char> {
        return rules.flatMap { it.allChars }.toSet()
    }

    fun findMatches(text: String): List<LanguageRule> {
        if (text.isEmpty()) return emptyList()

        val cleanText = text.lowercase().filter { it.isLetter() }
        if (cleanText.isEmpty()) return emptyList()

        val matches = mutableListOf<Pair<LanguageRule, Double>>()

        for (rule in rules) {
            var score = rule.calculateMatchScore(text)
            if (score > 0) {
                // Boost score if has unique characters
                if (rule.uniqueChars.isNotEmpty() && cleanText.any { it in rule.uniqueChars }) {
                    score = 1.0 // Maximum score for unique chars
                }
                matches.add(rule to score)
            }
        }

        // Sort by score descending
        return matches.sortedByDescending { it.second }.map { it.first }
    }
}
