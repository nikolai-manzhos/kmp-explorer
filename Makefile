generateSampleAppGraph:
	./gradlew sample:composeApp:exploreGraph

# gradle plugin shortcuts
publish:
	./gradlew tool:gradle-plugin:publishToMavenLocal
build:
	./gradlew tool:gradle-plugin:build
test:
	./gradlew tool:gradle-plugin:check
