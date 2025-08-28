PYTHON_DATA_DIR = ../syllabreak-python/syllabreak/data
KOTLIN_RESOURCES_DIR = src/main/resources
KOTLIN_TEST_RESOURCES_DIR = src/test/resources

.PHONY: build test lint clean install sync-yaml

build:
	./gradlew build

test:
	./gradlew test

lint:
	./gradlew ktlintCheck

lint-fix:
	./gradlew ktlintFormat

clean:
	./gradlew clean

install:
	./gradlew wrapper --gradle-version=8.7

sync-yaml:
	mkdir -p $(KOTLIN_RESOURCES_DIR) $(KOTLIN_TEST_RESOURCES_DIR)
	cp $(PYTHON_DATA_DIR)/rules.yaml $(KOTLIN_RESOURCES_DIR)/
	cp $(PYTHON_DATA_DIR)/syllabify_tests.yaml $(KOTLIN_TEST_RESOURCES_DIR)/
	cp $(PYTHON_DATA_DIR)/detect_language_tests.yaml $(KOTLIN_TEST_RESOURCES_DIR)/