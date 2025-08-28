package fm.apakabar.syllabreak

data class LanguageRule(
    val lang: String,
    val vowels: Set<Char>,
    val consonants: Set<Char>,
    val sonorants: Set<Char>,
    val clustersKeepNext: Set<String>,
    val dontSplitDigraphs: Set<String>,
    val digraphVowels: Set<String>,
    val glides: Set<Char>,
    val syllabicConsonants: Set<Char>,
    val modifiersAttachLeft: Set<Char>,
    val modifiersAttachRight: Set<Char>,
    val modifiersSeparators: Set<Char>,
    val clustersOnlyAfterLong: Set<String> = emptySet()
) {
    val allChars: Set<Char> = vowels + consonants + modifiersAttachLeft + 
                               modifiersAttachRight + modifiersSeparators
    
    var uniqueChars: Set<Char> = emptySet()
    lateinit var meta: MetaRule
    
    constructor(data: Map<String, Any>) : this(
        lang = data["lang"] as String,
        vowels = (data["vowels"] as String).toSet(),
        consonants = (data["consonants"] as String).toSet(),
        sonorants = (data["sonorants"] as String).toSet(),
        clustersKeepNext = (data["clusters_keep_next"] as? List<*>)?.map { it.toString() }?.toSet() ?: emptySet(),
        dontSplitDigraphs = (data["dont_split_digraphs"] as? List<*>)?.map { it.toString() }?.toSet() ?: emptySet(),
        digraphVowels = (data["digraph_vowels"] as? List<*>)?.map { it.toString() }?.toSet() ?: emptySet(),
        glides = (data["glides"] as? String)?.toSet() ?: emptySet(),
        syllabicConsonants = (data["syllabic_consonants"] as? String)?.toSet() ?: emptySet(),
        modifiersAttachLeft = (data["modifiers_attach_left"] as? String)?.toSet() ?: emptySet(),
        modifiersAttachRight = (data["modifiers_attach_right"] as? String)?.toSet() ?: emptySet(),
        modifiersSeparators = (data["modifiers_separators"] as? String)?.toSet() ?: emptySet(),
        clustersOnlyAfterLong = (data["clusters_only_after_long"] as? List<*>)?.map { it.toString() }?.toSet() ?: emptySet()
    )
    
    fun calculateMatchScore(text: String): Double {
        if (text.isEmpty()) return 0.0
        
        val cleanText = text.lowercase().filter { it.isLetter() }
        if (cleanText.isEmpty()) return 0.0
        
        var matches = 0
        var total = 0
        
        for (char in cleanText) {
            if (char in allChars) {
                matches++
            }
            total++
        }
        
        return if (total > 0) matches.toDouble() / total else 0.0
    }
}