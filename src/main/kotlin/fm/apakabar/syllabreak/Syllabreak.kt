package fm.apakabar.syllabreak

import org.yaml.snakeyaml.Yaml
import java.io.InputStream

class Syllabreak @JvmOverloads constructor(
    private val softHyphen: String = "\u00AD"
) {
    private val metaRule: MetaRule = loadRules()
    
    private fun loadRules(): MetaRule {
        val yaml = Yaml()
        val input: InputStream = this::class.java.getResourceAsStream("/rules.yaml")
            ?: throw IllegalStateException("Cannot load rules.yaml")
        
        input.use {
            val data = yaml.load<Map<String, Any>>(it)
            val rulesData = data["rules"] as List<Map<String, Any>>
            val rules = rulesData.map { LanguageRule(it) }
            return MetaRule(rules)
        }
    }
    
    fun detectLanguage(text: String): List<String> {
        val matchingRules = metaRule.findMatches(text)
        return matchingRules.map { it.lang }
    }
    
    @JvmOverloads
    fun syllabify(text: String, lang: String? = null): String {
        if (text.isEmpty()) return text
        
        val rule = if (lang != null) {
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
        return metaRule.rules.find { it.lang == lang }
            ?: throw IllegalArgumentException("Language '$lang' is not supported")
    }
}