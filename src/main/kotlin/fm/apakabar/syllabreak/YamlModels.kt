package fm.apakabar.syllabreak

import com.fasterxml.jackson.annotation.JsonProperty

data class RulesYaml(
    val rules: List<RuleYaml>,
)

data class RuleYaml(
    val lang: String,
    val vowels: String,
    val consonants: String,
    val sonorants: String,
    @field:JsonProperty("clusters_keep_next")
    val clustersKeepNext: List<String>?,
    @field:JsonProperty("dont_split_digraphs")
    val dontSplitDigraphs: List<String>?,
    @field:JsonProperty("digraph_vowels")
    val digraphVowels: List<String>?,
    val glides: String?,
    @field:JsonProperty("syllabic_consonants")
    val syllabicConsonants: String?,
    @field:JsonProperty("modifiers_attach_left")
    val modifiersAttachLeft: String?,
    @field:JsonProperty("modifiers_attach_right")
    val modifiersAttachRight: String?,
    @field:JsonProperty("modifiers_separators")
    val modifiersSeparators: String?,
    @field:JsonProperty("clusters_only_after_long")
    val clustersOnlyAfterLong: List<String>?,
)
