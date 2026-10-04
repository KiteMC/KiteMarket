plugins { java }

group = "com.kitemc.examples"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

val sdkJar = providers.gradleProperty("marketApiJar")
    .orElse("../../market-api/build/libs/KiteMarket-API-1.0.0.jar")
dependencies {
    compileOnly(files(sdkJar.get()))
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    constraints { compileOnly("com.google.code.gson:gson:2.11.0") }
}
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(11)
}
tasks.named<JavaCompile>("compileJava") {
    doFirst {
        require(file(sdkJar.get()).isFile) {
            "Build :market-api:jar first, or pass -PmarketApiJar=<absolute SDK JAR path>."
        }
    }
}
tasks.processResources {
    filesMatching("plugin.yml") { expand("version" to project.version.toString()) }
}
tasks.jar { from("LICENSE") { into("META-INF") } }
tasks.register<Zip>("developerBundle") {
    dependsOn(tasks.jar)
    archiveBaseName.set("KiteMarket-API-Example")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from(tasks.jar)
    from("README.md", "README.en.md", "LICENSE", "build.gradle.kts", "settings.gradle.kts")
    from("src") { into("src") }
}
