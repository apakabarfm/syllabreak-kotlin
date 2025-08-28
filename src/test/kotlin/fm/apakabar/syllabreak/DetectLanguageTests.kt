package fm.apakabar.syllabreak

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.yaml.snakeyaml.Yaml

class DetectLanguageTests {
    @TestFactory
    fun detectLanguageTests(): Collection<DynamicTest> {
        val yaml = Yaml()
        val input =
            this::class.java.getResourceAsStream("/detect_language_tests.yaml")
                ?: throw IllegalStateException("Cannot load detect_language_tests.yaml")

        val data = input.use { yaml.load<Map<String, Any>>(it) }
        val testSections = data["tests"] as List<Map<String, Any>>

        val tests = mutableListOf<DynamicTest>()
        val syllabreak = Syllabreak()

        for (section in testSections) {
            val lang = section["lang"] as String
            val cases = section["cases"] as List<String>

            for (text in cases) {
                tests.add(
                    DynamicTest.dynamicTest("Detect: $text -> should contain $lang") {
                        val result = syllabreak.detectLanguage(text)
                        assert(lang in result) { "Failed for '$text': got $result, expected to contain $lang" }
                    },
                )
            }
        }

        return tests
    }
}
