package fm.apakabar.syllabreak

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.yaml.snakeyaml.Yaml
import kotlin.test.assertEquals

class SyllabifyTests {
    
    data class TestSection(
        val section: String,
        val lang: String?,
        val cases: List<TestCase>
    )
    
    data class TestCase(
        val text: String,
        val want: String
    )
    
    @TestFactory
    fun syllabifyTests(): Collection<DynamicTest> {
        val yaml = Yaml()
        val input = this::class.java.getResourceAsStream("/syllabify_tests.yaml")
            ?: throw IllegalStateException("Cannot load syllabify_tests.yaml")
        
        val data = input.use { yaml.load<Map<String, Any>>(it) }
        val testSections = data["tests"] as List<Map<String, Any>>
        
        val tests = mutableListOf<DynamicTest>()
        val syllabifier = Syllabreak("-") // Use regular hyphen for tests
        
        for (section in testSections) {
            val sectionName = section["section"] as String
            val lang = section["lang"] as String?
            val cases = section["cases"] as List<Map<String, Any>>
            
            for (case in cases) {
                val text = case["text"] as String
                val want = case["want"] as String
                
                tests.add(DynamicTest.dynamicTest("[$sectionName] $text -> $want") {
                    val result = if (lang != null) {
                        syllabifier.syllabify(text, lang)
                    } else {
                        syllabifier.syllabify(text)
                    }
                    assertEquals(want, result, "Failed for '$text': got '$result', want '$want'")
                })
            }
        }
        
        return tests
    }
}