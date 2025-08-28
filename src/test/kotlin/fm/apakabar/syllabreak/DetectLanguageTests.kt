package fm.apakabar.syllabreak

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class DetectLanguageTests {
    data class TestData(val tests: List<TestSection>)

    data class TestSection(val lang: String?, val cases: List<String>)

    @TestFactory
    fun detectLanguageTests(): Collection<DynamicTest> {
        val mapper = ObjectMapper(YAMLFactory()).registerModule(kotlinModule())
        val input =
            this::class.java.getResourceAsStream("/detect_language_tests.yaml")
                ?: throw IllegalStateException("Cannot load detect_language_tests.yaml")

        val data: TestData = input.use { mapper.readValue(it) }
        val tests = mutableListOf<DynamicTest>()
        val syllabreak = Syllabreak()

        for (section in data.tests) {
            for (text in section.cases) {
                val testName =
                    if (section.lang != null) {
                        "Detect: $text -> should contain ${section.lang}"
                    } else {
                        "Detect: $text -> should return empty"
                    }
                tests.add(
                    DynamicTest.dynamicTest(testName) {
                        val result = syllabreak.detectLanguage(text)
                        if (section.lang != null) {
                            assert(section.lang in result) { "Failed for '$text': got $result, expected to contain ${section.lang}" }
                        } else {
                            assert(result.isEmpty()) { "Failed for '$text': got $result, expected empty list" }
                        }
                    },
                )
            }
        }

        return tests
    }
}
