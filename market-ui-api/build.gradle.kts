plugins { `java-library` }

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    testImplementation("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    constraints {
        compileOnly("com.google.code.gson:gson:2.11.0")
        testImplementation("com.google.code.gson:gson:2.11.0")
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}
tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
tasks.withType<Jar>().configureEach {
    archiveBaseName.set("KiteMarket-UI-API")
    from("LICENSE") { into("META-INF") }
}
tasks.named<Jar>("jar") {
    manifest.attributes["Automatic-Module-Name"] = "com.kitemc.market.ui.api"
}
