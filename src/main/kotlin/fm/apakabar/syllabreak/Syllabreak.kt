package fm.apakabar.syllabreak

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.InputStream

class Syllabreak
    @JvmOverloads
    constructor(
        private val softHyphen: String = "\u00AD",
    ) {
        private val metaRule: MetaRule = loadRules()

        private fun loadRules(): MetaRule {
            val mapper = ObjectMapper(YAMLFactory()).registerModule(kotlinModule())
            val input = requireNotNull(
                this::class.java.getResourceAsStream("/rules.yaml")
            ) { "Cannot load rules.yaml" }

            val data: RulesYaml = input.use { mapper.readValue(it) }
            val rules =
                data.rules.map { ruleYaml ->
                    LanguageRule(
                        lang = ruleYaml.lang,
                        vowels = ruleYaml.vowels.toSet(),
                        consonants = ruleYaml.consonants.toSet(),
                        sonorants = ruleYaml.sonorants.toSet(),
                        clustersKeepNext = (ruleYaml.clustersKeepNext ?: emptyList()).toSet(),
                        dontSplitDigraphs = (ruleYaml.dontSplitDigraphs ?: emptyList()).toSet(),
                        digraphVowels = (ruleYaml.digraphVowels ?: emptyList()).toSet(),
                        glides = (ruleYaml.glides ?: "").toSet(),
                        syllabicConsonants = (ruleYaml.syllabicConsonants ?: "").toSet(),
                        modifiersAttachLeft = (ruleYaml.modifiersAttachLeft ?: "").toSet(),
                        modifiersAttachRight = (ruleYaml.modifiersAttachRight ?: "").toSet(),
                        modifiersSeparators = (ruleYaml.modifiersSeparators ?: "").toSet(),
                        clustersOnlyAfterLong = (ruleYaml.clustersOnlyAfterLong ?: emptyList()).toSet(),
                    )
                }
            return MetaRule(rules)
        }

        fun detectLanguage(text: String): List<String> {
            val matchingRules = metaRule.findMatches(text)
            return matchingRules.map { it.lang }
        }

        @JvmOverloads
        fun syllabify(
            text: String,
            lang: String? = null,
        ): String {
            if (text.isEmpty()) return text

            val rule =
                if (lang != null) {
                    getRuleByLang(lang)
                } else {
                    autoDetectRule(text) ?: return text
                }

            val syllabifier = WordSyllabifier(rule)
            val tokenizer = Tokenizer(rule)
            val tokens = tokenizer.tokenize(text)

            return tokens.joinToString("") { token ->
                when (token.type) {
                    TokenType.WORD -> syllabifier.syllabifyWord(token.text, softHyphen)
                    else -> token.text
                }
            }
        }

        private fun autoDetectRule(text: String): LanguageRule? {
            val matchingRules = metaRule.findMatches(text)
            return matchingRules.firstOrNull()
        }

        private fun getRuleByLang(lang: String): LanguageRule {
            return requireNotNull(metaRule.rules.find { it.lang == lang }) {
                "Language '$lang' is not supported"
            }
        }
    }
