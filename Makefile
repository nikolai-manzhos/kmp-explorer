generateSampleAppGraph:
	./gradlew sample:composeApp:exploreKmpGraph

# gradle plugin shortcuts
publish:
	./gradlew tool:gradle-plugin:publishToMavenLocal
assemble:
	./gradlew tool:gradle-plugin:assemble
test:
	./gradlew tool:gradle-plugin:check
