package fm.apakabar.syllabreak

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

class SyllabifyTests {
    data class TestData(val tests: List<TestSection>)

    data class TestSection(
        val section: String,
        val lang: String?,
        val cases: List<TestCase>,
    )

    data class TestCase(
        val text: String,
        val want: String,
    )

    @TestFactory
    fun syllabifyTests(): Collection<DynamicTest> {
        val mapper = ObjectMapper(YAMLFactory()).registerModule(kotlinModule())
        val input = requireNotNull(
            this::class.java.getResourceAsStream("/syllabify_tests.yaml")
        ) { "Cannot load syllabify_tests.yaml" }

        val data: TestData = input.use { mapper.readValue(it) }
        val tests = mutableListOf<DynamicTest>()
        val syllabifier = Syllabreak("-") // Use regular hyphen for tests

        for (section in data.tests) {
            for (case in section.cases) {
                tests.add(
                    DynamicTest.dynamicTest("[${section.section}] ${case.text} -> ${case.want}") {
                        val result =
                            if (section.lang != null) {
                                syllabifier.syllabify(case.text, section.lang)
                            } else {
                                syllabifier.syllabify(case.text)
                            }
                        assertEquals(case.want, result, "Failed for '${case.text}': got '$result', want '${case.want}'")
                    },
                )
            }
        }

        return tests
    }
}
