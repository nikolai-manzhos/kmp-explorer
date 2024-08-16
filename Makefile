generateSampleAppGraph:
	./gradlew sample:composeApp:exploreGraph

# gradle plugin shortcuts
publishToMavenLocal:
	./gradlew tool:gradle-plugin:publishToMavenLocal
buildPlugin:
	./gradlew tool:gradle-plugin:build
