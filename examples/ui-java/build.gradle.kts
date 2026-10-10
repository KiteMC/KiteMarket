plugins { java }

group = "com.kitemc.examples"
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

val sdkJar = providers.gradleProperty("uiApiJar")
    .orElse("../../market-ui-api/build/libs/KiteMarket-UI-API-1.1.0.jar")

dependencies {
    compileOnly(files(sdkJar.get()))
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("beer.devs:itemsadder-api:4.0.18-beta-10")
    constraints { compileOnly("com.google.code.gson:gson:2.11.0") }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.named<JavaCompile>("compileJava") {
    doFirst {
        require(file(sdkJar.get()).isFile) {
            "Build :market-ui-api:jar first, or pass -PuiApiJar=<absolute SDK JAR path>."
        }
    }
}

tasks.processResources {
    filesMatching("plugin.yml") { expand("version" to project.version.toString()) }
}

tasks.jar {
    from("LICENSE") { into("META-INF") }
}

tasks.register<Zip>("developerBundle") {
    dependsOn(tasks.jar)
    archiveBaseName.set("KiteMarket-IA-Example")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from(tasks.jar)
    from("theme.yml", "README.md", "LICENSE", "build.gradle.kts", "settings.gradle.kts")
    from("src") { into("src") }
    val resourceDirectory = file("../ui/themes/example-ia/itemsadder")
        .takeIf { it.isDirectory } ?: file("itemsadder")
    from(resourceDirectory) { into("itemsadder") }
}
